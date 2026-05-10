import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Profile } from './pages/profile/profile';
import { Rankings } from './pages/rankings/rankings';
import { Login } from './pages/login/login';
import { Perfil } from './pages/perfil/perfil';

export const routes: Routes = [
  { path: '',                  component: Home },
  { path: 'profile/:username', component: Profile },
  { path: 'rankings',          component: Rankings },
  { path: 'login',             component: Login },
  { path: 'perfil',            component: Perfil },
  { path: '**',                redirectTo: '' },
];