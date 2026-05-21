import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PlayerStatsService } from '../../services/player-stats.service';
import { BtnPerfilComponent } from '../../components/btn-perfil/btn-perfil';

@Component({
  selector: 'app-registro-clan',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, BtnPerfilComponent],
  templateUrl: './registro-clan.html',
  styleUrl: './registro-clan.css',
})
export class RegistroClan {

  nombreClan = '';
  error = '';
  mensaje = '';
  // Variable para validar cuando se ha realizado la accion y cuando a fallado o esta en proceso
  cargando = false;

  constructor(private playerStatsService: PlayerStatsService) {}

  // Metodo para crear un clan, comprobando que el nombre no este vacio
  crear(): void {
    this.error = '';
    this.mensaje = '';

    if (!this.nombreClan.trim()) {
      this.error = 'Introduce un nombre para el clan.';
      return;
    }

    this.cargando = true;

    // Llamada al servicio, al metodo para crear el clan pasandole el nombre pro parametro
    this.playerStatsService.crearClan(this.nombreClan.trim()).subscribe({
      next: (resultado) => {
        this.cargando = false;
        if (resultado) {
          this.mensaje = `¡Clan "${this.nombreClan}" creado correctamente!`;
          this.nombreClan = '';
        } else {
          this.error = 'No se pudo crear el clan. Inténtalo de nuevo.';
        }
      },
      error: () => {
        this.cargando = false;
        this.error = 'Error al conectar con el servidor.';
      }
    });
  }
}