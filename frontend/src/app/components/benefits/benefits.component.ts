import {Component, inject, signal, OnInit, computed} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BenefitService } from '../../services/benefit.service';
import { Benefit } from '../../models/benefit.model';
import { BenefitFormComponent } from '../benefit-form/benefit-form.component';

@Component({
  selector: 'app-benefits',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, BenefitFormComponent],
  templateUrl: './benefits.component.html',
  styleUrl: './benefits.component.scss'
})
export class BenefitsComponent implements OnInit {
  private readonly benefitService = inject(BenefitService);

  benefits = signal<Benefit[]>([]);
  totalPages = signal(0);
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);

  isFormOpen = signal(false);
  isDeleteDialogOpen = signal(false);
  isTransferDialogOpen = signal(false);

  selectedBenefit = signal<Benefit | null>(null);
  selectedUserId = signal<number | null>(null);
  filteredBenefits = computed(() => {
    const fromId = this.selectedBenefit()?.id ?? null;
    const list = this.benefits();

    if (fromId == null) return list;

    return list.filter(b => b.id !== fromId);
  });

  transferAmount = signal<number | null>(null);

  isTransferAmountInvalid = computed(() => {
    const amount = this.transferAmount();
    const selected = this.selectedBenefit();

    if (!selected) return true;
    if (amount == null) return true;
    if (!Number.isFinite(amount)) return true;
    if (amount <= 0) return true;

    const max = Number(selected.valor ?? 0);
    return amount > max;
  });

  transferAmountError = computed(() => {
    const selected = this.selectedBenefit();
    const amount = this.transferAmount();

    if (!selected) return null;
    if (amount == null) return 'Informe o valor da transferência';
    if (!Number.isFinite(amount) || amount <= 0) return 'O valor deve ser maior que zero';

    const max = Number(selected.valor ?? 0);
    if (amount > max) return `O valor não pode ser maior que ${max}`;
    return null;
  });

  isLoading = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadBenefits();
  }

  loadBenefits(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.benefitService.getAll(this.currentPage(), this.pageSize()).subscribe({
      next: (response) => {
        this.benefits.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar benefícios');
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }

  openCreateForm(): void {
    this.selectedBenefit.set(null);
    this.isFormOpen.set(true);
  }

  openEditForm(benefit: Benefit): void {
    this.selectedBenefit.set(benefit);
    this.isFormOpen.set(true);
  }

  closeForm(): void {
    this.isFormOpen.set(false);
    this.selectedBenefit.set(null);
  }

  onFormSubmit(benefitData: Omit<Benefit, 'id'>): void {
    const selected = this.selectedBenefit();

    if (selected) {
      this.benefitService.update(selected.id, benefitData).subscribe({
        next: () => {
          this.loadBenefits();
          this.closeForm();
        },
        error: (err) => this.error.set('Erro ao atualizar benefício')
      });
    } else {
      this.benefitService.create(benefitData).subscribe({
        next: () => {
          this.loadBenefits();
          this.closeForm();
        },
        error: (err) => this.error.set('Erro ao criar benefício')
      });
    }
  }

  openDeleteDialog(benefit: Benefit): void {
    this.selectedBenefit.set(benefit);
    this.isDeleteDialogOpen.set(true);
  }

  closeDeleteDialog(): void {
    this.isDeleteDialogOpen.set(false);
    this.selectedBenefit.set(null);
  }

  confirmDelete(): void {
    const selected = this.selectedBenefit();
    if (!selected) return;

    this.benefitService.delete(selected.id).subscribe({
      next: () => {
        this.loadBenefits();
        this.closeDeleteDialog();
      },
      error: (err) => this.error.set('Erro ao excluir benefício')
    });
  }

  openTransferDialog(benefit: Benefit): void {
    this.selectedBenefit.set(benefit);
    this.selectedUserId.set(null);
    this.transferAmount.set(null);
    this.isTransferDialogOpen.set(true);
  }

  closeTransferDialog(): void {
    this.isTransferDialogOpen.set(false);
    this.selectedBenefit.set(null);
    this.selectedUserId.set(null);
    this.transferAmount.set(null);
  }

  confirmTransfer(): void {
    const selected = this.selectedBenefit();
    const userId = this.selectedUserId();
    const amount = this.transferAmount()!;
    if (!selected || !userId) return;

    this.benefitService.transfer({fromId: selected.id, toId: userId, amount: amount}).subscribe({
      next: () => {
        this.loadBenefits();
        this.closeTransferDialog();
      },
      error: (err) => this.error.set('Erro ao transferir benefício')
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadBenefits();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }
}