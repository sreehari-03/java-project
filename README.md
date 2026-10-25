# Student Result Management System — JavaFX application

Requires JDK 17+ and Maven 3.8+.

    mvn clean javafx:run

Demo logins: student / student123 · teacher / teacher123 (or register a new account).

## Account database

New accounts are stored in MySQL and can be used to log in after restarting the
application. Start a local MySQL server before creating an account. On its first
connection the application creates the `srms_db` database (if needed) and the
account/student tables.

The defaults are `localhost:3306`, database `srms_db`, user `root`, and an empty
password. Configure a different connection with the `SRMS_DB_URL`,
`SRMS_DB_USER`, and `SRMS_DB_PASSWORD` environment variables. For example, in
PowerShell:

    $env:SRMS_DB_USER = "your-user"
    $env:SRMS_DB_PASSWORD = "your-password"
    mvn javafx:run

## Layout
- `App`, `Nav` — entry point and screen switching (Login <-> Register <-> Teacher/Student portals)
- `resources/fxml/*.fxml` — LoginView, RegisterView, TeacherPortalView, StudentPortalView
- `resources/css/style.css` — design system (palette, buttons, inputs, tables, badges, charts)
- `controller/*` — one controller per view
- `data/DataStore` — in-memory data so the UI runs stand-alone. Replace its methods with JDBC/MySQL
  DAO calls (the mysql-connector-j dependency is already in the pom). Controllers only talk to this class.
