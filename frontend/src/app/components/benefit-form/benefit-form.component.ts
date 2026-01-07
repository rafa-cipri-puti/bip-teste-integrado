import { Component, EventEmitter, Input, Output, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Benefit } from '../../models/benefit.model';

@Component({
  selector: 'app-benefit-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
      <div class="form-group">
        <label for="nome">Nome</label>
        <input id="nome" type="text" formControlName="nome" class="form-control" />
        @if (form.get('nome')?.invalid && form.get('nome')?.touched) {
          <span class="error">Nome é obrigatório</span>
        }
      </div>

      <div class="form-group">
        <label for="descricao">Descrição</label>
        <textarea id="descricao" formControlName="descricao" class="form-control" rows="3"></textarea>
        @if (form.get('descricao')?.invalid && form.get('descricao')?.touched) {
          <span class="error">Descrição é obrigatória</span>
        }
      </div>

      <div class="form-group">
        <label for="valor">Valor</label>
        <input id="valor" type="number" formControlName="valor" class="form-control" step="0.01" />
        @if (form.get('valor')?.invalid && form.get('valor')?.touched) {
          <span class="error">Valor deve ser maior que zero</span>
        }
      </div>

      <div class="form-group checkbox-group">
        <label>
          <input type="checkbox" formControlName="ativo" />
          Ativo
        </label>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" (click)="cancel.emit()">Cancelar</button>
        <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Salvar</button>
      </div>
    </form>
  `,
  styles: [`
    .form {
      padding: 1.5rem;
    }

    .form-group {
      margin-bottom: 1rem;

      label {
        display: block;
        margin-bottom: 0.5rem;
        font-weight: 500;
        color: #374151;
      }

      .form-control {
        width: 100%;
        padding: 0.5rem 0.75rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 1rem;

        &:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }

      .error {
        color: #dc2626;
        font-size: 0.875rem;
        margin-top: 0.25rem;
      }
    }

    .checkbox-group label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;

      input {
        width: 1rem;
        height: 1rem;
      }
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 0.75rem;
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid #e5e7eb;
    }

    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &-primary {
        background: #3b82f6;
        color: white;
      }

      &-secondary {
        background: #e5e7eb;
        color: #374151;
      }
    }
  `]
})
export class BenefitFormComponent implements OnInit {
  @Input() benefit: Benefit | null = null;
  @Output() submitForm = new EventEmitter<Omit<Benefit, 'id'>>();
  @Output() cancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);

  form: FormGroup = this.fb.group({
    nome: ['', Validators.required],
    descricao: ['', Validators.required],
    valor: [0, [Validators.required, Validators.min(0.01)]],
    ativo: [true]
  });

  ngOnInit(): void {
    if (this.benefit) {
      this.form.patchValue(this.benefit);
    }
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.submitForm.emit(this.form.value);
    }
  }
}