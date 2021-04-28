# SpringPokerServer

#
This is from 'arafat' branch
#
Poker server with spring
#


Add below dependencies inside 

              <dependencies>
                     ....
                     .....
                     .....
              </dependencies> 

section on 'pom.xml' file of your project:

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
#
Now need to do one more thing.

change your application.properties file as follows:

       spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
       spring.datasource.username= your_username
       spring.datasource.password=your_password
       server.port=8080
       # The SQL dialect makes Hibernate generate better SQL for the chosen database
       spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect

       # Hibernate ddl auto (create, create-drop, validate, update)
       spring.jpa.hibernate.ddl-auto = update
       
#
Now copy below packages to your project src/main/java/ directory:

'db','model','exception', 'repository'

and adjust package names as per your project.
#
Now your are set to go....


You may use 
   
     DB db = DBFactory.getDB() 
for database connections. 

Now you may make queries such as:

    
#
    db.getuserID("JKALjsld","google");
    db.createUser( thirdparty_id,  username,  id_type);

#

N.B: please adjust package names in "db", "model" and "repository" package classes.
