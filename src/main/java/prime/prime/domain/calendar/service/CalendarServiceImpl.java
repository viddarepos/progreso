package prime.prime.domain.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import prime.prime.domain.calendar.mapper.CalendarMapper;
import prime.prime.domain.googleauthorization.entity.GoogleAuthorization;
import prime.prime.domain.googleauthorization.service.GoogleAuthorizationServiceImpl;
import prime.prime.infrastructure.exception.GoogleAuthorizationException;

@Service
public class CalendarServiceImpl implements CalendarService {

    private final String applicationName;
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String calendarId;
    private final GoogleAuthorizationServiceImpl googleAuthorizationService;
    private final CalendarMapper calendarMapper;

    @Autowired
    public CalendarServiceImpl(@Value("${google.service.app.name}") String applicationName,
        @Value("${google.service.calendar.id}") String calendarId,
        GoogleAuthorizationServiceImpl googleAuthorizationService,
        CalendarMapper calendarMapper) {
        this.applicationName = applicationName;
        this.calendarId = calendarId;
        this.googleAuthorizationService = googleAuthorizationService;
        this.calendarMapper = calendarMapper;
    }

    public Calendar createConnection() {
        NetHttpTransport httpTransport;
        GoogleAuthorization googleAuthorization;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            googleAuthorization = googleAuthorizationService.getFirstRecordFromGoogleAuthorizationTable();
            if (LocalDateTime.now().isAfter(googleAuthorization.getExpirationDateTime())) {
                googleAuthorizationService.refreshAccessTokenAndSave(googleAuthorization);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleAuthorizationException(
                "Invalid credentials.Cannot authenticate.Could not connect to google application.");
        }
        HttpRequestInitializer httpRequestInitializer = googleAuthorizationService.createAccessToken(
            googleAuthorization.getAccessToken(),
            googleAuthorization.getExpirationDateTime());
        return new Calendar.Builder(httpTransport, JSON_FACTORY,
            httpRequestInitializer).setApplicationName(applicationName).build();
    }

    private void setRecurrence(Event event) {
        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList(recurrence));
    }

    private void setRemindersOnEvent(Event event) {
        EventReminder[] reminderOverrides = new EventReminder[]{
            new EventReminder().setMethod("email").setMinutes(24 * 60),
            new EventReminder().setMethod("popup").setMinutes(10),};

        Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
            .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
    }

    @Override
    public String createEvent(prime.prime.domain.event.entity.Event eventToCreate) {
        Calendar calendar = createConnection();
        Event event = calendarMapper.progresoEventToGoogleEvent(eventToCreate);
        setRecurrence(event);
        setRemindersOnEvent(event);
        try {
            event = calendar.events().insert(calendarId, event).setSendNotifications(true)
                .setSendUpdates("all")
                .execute();
        } catch (IOException e) {
            throw new GoogleAuthorizationException("Failed to create google calendar event.");
        }
        return event.getId();
    }

    @Override
    public String updateEvent(prime.prime.domain.event.entity.Event eventToUpdate) {
        Calendar calendar = createConnection();
        Event event;
        try {
            calendar.events()
                .get(calendarId, eventToUpdate.getGoogleCalendarEventId())
                .execute();
        } catch (IOException e) {
            throw new GoogleAuthorizationException("Failed to update google calendar event.");
        }
        event = calendarMapper.progresoEventToGoogleEvent(eventToUpdate);

        try {
            calendar.events()
                .update(calendarId, eventToUpdate.getGoogleCalendarEventId(), event)
                .setSendUpdates("all").setAlwaysIncludeEmail(true).setSendNotifications(true)
                .execute();
        } catch (IOException e) {
            throw new GoogleAuthorizationException("Failed to update google calendar event.");
        }
        return event.getId();
    }

    @Override
    public void deleteEvent(prime.prime.domain.event.entity.Event eventToDelete) {
        Calendar calendar = createConnection();

        try {
            calendar.events().get(calendarId, eventToDelete.getGoogleCalendarEventId());
        } catch (IOException e) {
            throw new GoogleAuthorizationException("No such google calendar event.");
        }
        try {
            calendar.events().delete(calendarId, eventToDelete.getGoogleCalendarEventId())
                .setSendUpdates("all").setSendNotifications(true)
                .execute();
        } catch (IOException e) {
            throw new GoogleAuthorizationException("Failed to delete google calendar event.");
        }
    }
}
