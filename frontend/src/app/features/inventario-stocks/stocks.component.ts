import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService } from '../../core/services/stock.service';
import { MovimientoService } from '../../core/services/movimiento.service';
import { Stock } from '../../core/models/stock.model';
import { Almacen } from '../../core/models/maestro.model';

@Component({
  selector: 'app-stocks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stocks.component.html',
  styleUrl: './stocks.component.css'
})
export class StocksComponent implements OnInit {
  private stockService = inject(StockService);
  private movimientoService = inject(MovimientoService);

  stocks: Stock[] = [];
  filteredStocks: Stock[] = [];
  almacenes: Almacen[] = [];

  // Filtros
  searchTerm: string = '';
  selectedAlmacenId: string = '';
  loading: boolean = false;

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading = true;
    
    // Cargar almacenes para el filtro
    this.movimientoService.listarAlmacenes().subscribe({
      next: (data) => {
        this.almacenes = data;
      },
      error: (err) => console.error('Error al cargar almacenes', err)
    });

    // Cargar existencias físicas
    this.stockService.listarExistencias().subscribe({
      next: (data) => {
        this.stocks = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar existencias', err);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredStocks = this.stocks.filter(item => {
      const matchSearch = 
        item.producto.nombre.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        item.producto.sku.toLowerCase().includes(this.searchTerm.toLowerCase());
        
      const matchAlmacen = 
        !this.selectedAlmacenId || 
        item.almacen.id?.toString() === this.selectedAlmacenId;

      return matchSearch && matchAlmacen;
    });
  }

  getValuacionTotal(): number {
    return this.filteredStocks.reduce((total, item) => total + (item.cantidad * item.producto.precioVenta), 0);
  }
}
