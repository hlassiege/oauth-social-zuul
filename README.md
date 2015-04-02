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


To login, you have to insert a record manually in simpleAccount 

db.simpleAccount.insert(
{
    "_id" : ObjectId("551d6197cbd5baa14fa2c3cc"),
    "@class" : "com.lateralthoughts.commons.account.SimpleAccount",
    "email" : "some@one.com",
    "password" : "7284291fb9c4581e17c34e17516128339a4473ca973367b61f562405efce527e",
    "firstName" : "Hugo",
    "lastName" : "L",
    "locked" : true,
    "cguAccepted" : true,
    "enabled" : false,
    "admin" : false,
    "lastConnection" : ISODate("2015-04-02T15:34:47.768Z")
})

user/password = some@one.com  /  password
