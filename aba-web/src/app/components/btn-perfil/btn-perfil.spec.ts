import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BtnPerfilComponent as BtnPerfil } from './btn-perfil';

describe('BtnPerfil', () => {
  let component: BtnPerfil;
  let fixture: ComponentFixture<BtnPerfil>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BtnPerfil],
    }).compileComponents();

    fixture = TestBed.createComponent(BtnPerfil);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
