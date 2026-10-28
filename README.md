# Student Result Management System — JavaFX application

Requires JDK 17+ and Maven 3.8+.

    mvn clean javafx:run

Demo login: teacher / teacher123 (or register a new account).

## Account database

New accounts are stored in MySQL and can be used to log in after restarting the
application. Student results and teacher remarks are also stored in MySQL and
remain available after restarting. Start a local MySQL server before using the
application. On its first connection it creates the `srms_db` database (if
needed) and the account, student, and result tables.

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
- `data/DataStore` — shared application state; result and remark data is loaded
  from and saved to MySQL through the JDBC services.
