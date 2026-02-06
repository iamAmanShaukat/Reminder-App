package com.example.reminder.data;

import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderDao;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class BackupRepository {

    private final ReminderDao reminderDao;

    @Inject
    public BackupRepository(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
    }

    public String exportRemindersToJson() {
        try {
            List<Reminder> reminders = reminderDao.getAllRemindersSync(); // Need to ensure this exists or use
                                                                          // getActiveRemindersSync
            JSONArray jsonArray = new JSONArray();

            for (Reminder r : reminders) {
                JSONObject obj = new JSONObject();
                obj.put("id", r.getId());
                obj.put("title", r.getTitle());
                obj.put("description", r.getDescription());
                obj.put("timeMillis", r.getTimeMillis());
                obj.put("isCompleted", r.isCompleted());
                // Add any other fields if they exist in Entity
                jsonArray.put(obj);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            android.util.Log.e("BackupRepository", "Error exporting JSON", e);
            return null;
        }
    }

    public void importRemindersFromJson(String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            List<Reminder> reminders = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Reminder r = new Reminder();
                // We might want to clear ID to let AutoGenerate handle it,
                // OR duplicate check. For simple restore, we can try to insert
                // If conflict strategy is REPLACE, it will overwrite same IDs.

                // r.setId(obj.optInt("id")); // Optional: Keep ID or generate new?
                r.setTitle(obj.optString("title"));
                r.setDescription(obj.optString("description"));
                r.setTimeMillis(obj.optLong("timeMillis"));
                r.setCompleted(obj.optBoolean("isCompleted"));

                reminders.add(r);
            }

            // Batch insert
            for (Reminder r : reminders) {
                reminderDao.insertReminder(r);
            }

        } catch (Exception e) {
            android.util.Log.e("BackupRepository", "Error importing JSON", e);
        }
    }

    public interface BackupCallback {
        void onSuccess();

        void onError(Exception e);
    }

    public void uploadToDrive(android.content.Context context,
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account, String jsonContent,
            BackupCallback callback) {
        new Thread(() -> {
            try {
                com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
                        .usingOAuth2(
                                context, java.util.Collections
                                        .singleton(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(account.getAccount());

                com.google.api.services.drive.Drive googleDriveService = new com.google.api.services.drive.Drive.Builder(
                        com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport(),
                        new com.google.api.client.json.gson.GsonFactory(),
                        credential)
                        .setApplicationName("Reminder App")
                        .build();

                // Check for existing file to update or create new
                com.google.api.services.drive.model.FileList result = googleDriveService.files().list()
                        .setSpaces("appDataFolder")
                        .setQ("name = 'reminder_backup.json' and trashed = false")
                        .setFields("files(id, name)")
                        .execute();

                List<com.google.api.services.drive.model.File> files = result.getFiles();

                // Content
                com.google.api.client.http.ByteArrayContent content = new com.google.api.client.http.ByteArrayContent(
                        "application/json", jsonContent.getBytes());

                if (files != null && !files.isEmpty()) {
                    // Update existing
                    String fileId = files.get(0).getId();
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName("reminder_backup.json"); // Redundant but safe

                    googleDriveService.files().update(fileId, fileMetadata, content).execute();
                } else {
                    // Create new
                    com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                    fileMetadata.setName("reminder_backup.json");
                    fileMetadata.setParents(java.util.Collections.singletonList("appDataFolder"));

                    googleDriveService.files().create(fileMetadata, content)
                            .setFields("id")
                            .execute();
                }

                // Notify internal success (UI thread handling needed in caller or here with
                // Handler, calling back on background thread is safer for repo)
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public void restoreFromDrive(android.content.Context context,
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account,
            BackupCallback callback) {
        new Thread(() -> {
            try {
                com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential credential = com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
                        .usingOAuth2(
                                context, java.util.Collections
                                        .singleton(com.google.api.services.drive.DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(account.getAccount());

                com.google.api.services.drive.Drive googleDriveService = new com.google.api.services.drive.Drive.Builder(
                        com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport(),
                        new com.google.api.client.json.gson.GsonFactory(),
                        credential)
                        .setApplicationName("Reminder App")
                        .build();

                // Find file
                com.google.api.services.drive.model.FileList result = googleDriveService.files().list()
                        .setSpaces("appDataFolder")
                        .setQ("name = 'reminder_backup.json' and trashed = false")
                        .setFields("files(id, name)")
                        .execute();

                List<com.google.api.services.drive.model.File> files = result.getFiles();

                if (files == null || files.isEmpty()) {
                    throw new Exception("No backup found in Drive.");
                }

                String fileId = files.get(0).getId();

                // Download
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                googleDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                String jsonString = new String(outputStream.toByteArray());

                // Import
                importRemindersFromJson(jsonString);

                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
