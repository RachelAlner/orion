import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import AppLayout from "./components/layout/AppLayout";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import ProjectsPage from "./pages/ProjectsPage";
import RegisterPage from "./pages/RegisterPage";
import SchedulePage from "./pages/SchedulePage";
import ProtectedRoute from "./components/ProtectedRoute";
import HomeRedirect from "./components/HomeRedirect";
import ProjectDetailPage from "./pages/ProjectDetailPage";

function App() {
  return (
    <BrowserRouter>
        <Routes>
            <Route 
                path="/"
                element={<HomeRedirect />}
            />

            <Route 
                path="/login"
                element={<LoginPage />}
            />

            <Route 
                path="/register"
                element={<RegisterPage />}
            />

            <Route element={<ProtectedRoute />}>
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
                        path="/projects/:projectId"
                        element={<ProjectDetailPage />}
                    />

                    <Route 
                        path="/schedule"
                        element={<SchedulePage />}
                    />
                </Route>
            </Route>

                <Route 
                    path="*"
                    element={
                        <Navigate
                            to="/"
                            replace 
                        />
                    }
                />
        </Routes>
    </BrowserRouter>
  );
}

export default App;