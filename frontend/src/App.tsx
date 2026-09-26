import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import AppLayout from "./components/AppLayout";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import ProjectsPage from "./pages/ProjectsPage";
import RegisterPage from "./pages/RegisterPage";
import SchedulePage from "./pages/SchedulePage";

function App() {
  return (
    <BrowserRouter>
        <Routes>
            <Route 
                path="/"
                element={<Navigate to="/dashboard" replace />}
            />

            <Route 
                path="/login"
                element={<LoginPage />}
            />

            <Route 
                path="/register"
                element={<RegisterPage />}
            />

            <Route element={<AppLayout />}>
                <Route 
                    path="/dashboard"
                    element={<DashboardPage />}
                />

                <Route 
                    path="/projects"
                    element={<ProjectsPage />}
                />

                <Route 
                    path="/schedule"
                    element={<SchedulePage />}
                />
            </Route>
        </Routes>
    </BrowserRouter>
  );
}

export default App;