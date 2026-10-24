# Student Result Management System — JavaFX front end

Requires JDK 17+ and Maven 3.8+.

    mvn clean javafx:run

Demo logins: student / student123 · teacher / teacher123 (or register a new account).

## Layout
- `App`, `Nav` — entry point and screen switching (Login <-> Register <-> Teacher/Student portals)
- `resources/fxml/*.fxml` — LoginView, RegisterView, TeacherPortalView, StudentPortalView
- `resources/css/style.css` — design system (palette, buttons, inputs, tables, badges, charts)
- `controller/*` — one controller per view
- `data/DataStore` — in-memory data so the UI runs stand-alone. Replace its methods with JDBC/MySQL
  DAO calls (the mysql-connector-j dependency is already in the pom). Controllers only talk to this class.
