# KRY code assignment

Issues Handled

- On restart of server, the added services will be available as data persisted into Database
- User can able to delete the added service
- User can able to name services and remember when they were added
- The HTTP poller is implemented

Frontend Features:
- Full create/delete functionality for services
- The results from the poller displayed automatically to the user.

Backend Features:

- Service URL's are validated.

# Steps to Follow

```
1. Run DB Migration to create poller.db and table service.
2. Deploy Mainverticle by running Start class
3. Run the application - http://localhost:8066
```

Run gradle directly from the command line:
```
./gradlew clean run
```

Sample service urls to add & test:
```
- Oxford - https://developer.oxforddictionaries.com/
- File.io - https://www.file.io/
- GeoAPI - https://api.gouv.fr/les-api/api-geo
- BetterDoctor - https://developer.betterdoctor.com/
- Covid19 - https://covid19api.com/

```
