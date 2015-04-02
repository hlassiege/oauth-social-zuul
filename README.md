Strongly inspired from https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v

There is some differences :

- spring social is used to let the user choose between a classic login form and a social login (facebook)
- jwt is not used
- mongodb is used as a storage for users and tokens 
- crsf is disabled (mainly because I wasn't sure how it worked)


How to launch the application(s)

- Run a mongodb server
- Run spring-boot:run in each folder
- go to http://localhost:8080
