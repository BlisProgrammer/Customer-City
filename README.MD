# Customer City App
Aim: Publish an Android app to Google play store with firebase analytics. To test the tracking features of google analytics on native app environments.

## Application details
- Language: Chinese (Hong Kong)
- Operating System: Android (with android.permission.INTERNET)

## Pages (Activities)
### 1. Main - A container for all major fragments
1. Home Fragment
    - Welcoming screen that have the same content as about us.
2. Search Fragment
    - Includes filtering for company names.
3. Add Fragment
    - Fragment to add locally saved record.
4. Saved Fragment
    - View saved records for both cloud or local save.
5. User Fragment
    - Fragment for account login, reset password, and logout.

#### Hidden Fragments (Fragments that aren't shown in bottom navigation bar)
1. Result Fragment
    - Fragment for search result by company name.
2. Record Fragment
    - Fragment for showing record, allows for sharing and saving records.

### 2. About
Fragment for user to learn more about us.
### 3. Help
Fragment for Frequently asked questions.

## Firebase Integration
### Default Events
1. app_open
2. screen_view (Between activities)
### Manually added Events
1. button_click
2. item_click
3. app_open
4. screen_view (Between fragments)










