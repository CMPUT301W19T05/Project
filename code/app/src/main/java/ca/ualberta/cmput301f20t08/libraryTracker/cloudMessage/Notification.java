package ca.ualberta.cmput301f20t08.libraryTracker.cloudMessage;
/**
 * Configure a notification
 */
public class Notification {
    public String body;
    public String title;

    public Notification(String body, String title) {
        this.body = body;
        this.title = title;
    }
}
