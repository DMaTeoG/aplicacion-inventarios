import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StockService } from '../../core/services/stock.service';
import { ProductService } from '../../core/services/product.service';
import { MovimientoService } from '../../core/services/movimiento.service';
import { StocksComponent } from '../inventario-stocks/stocks.component';
import { ProductosComponent } from '../productos/productos.component';
import { MovimientosComponent } from '../movimientos/movimientos.component';
import { Stock } from '../../core/models/stock.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    StocksComponent, 
    ProductosComponent, 
    MovimientosComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private stockService = inject(StockService);
  private productService = inject(ProductService);
  private router = inject(Router);

  // Tab activo actual
  activeTab: 'dashboard' | 'stocks' | 'productos' | 'movimientos' = 'dashboard';
  username: string = '';

  // Métricas Consolidadas
  totalProductos: number = 0;
  totalExistencias: number = 0;
  valuacionGlobal: number = 0;
  totalAlmacenes: number = 0;
  
  loadingMetrics: boolean = false;
  recentStocks: Stock[] = [];

  ngOnInit(): void {
    this.username = this.authService.getUsername();
    this.cargarMetricas();
  }

  cargarMetricas(): void {
    this.loadingMetrics = true;
    
    // Cargar existencias físicas para consolidar totales
    this.stockService.listarExistencias().subscribe({
      next: (stocks) => {
        this.recentStocks = stocks.slice(0, 5); // Tomar 5 registros de muestra
        
        // Sumar existencias totales
        this.totalExistencias = stocks.reduce((sum, item) => sum + item.cantidad, 0);
        
        // Valuación Global
        this.valuacionGlobal = stocks.reduce((sum, item) => sum + (item.cantidad * item.producto.precioVenta), 0);
        
        // Almacenes Únicos
        const idsAlmacen = new Set(stocks.map(s => s.almacen.id));
        this.totalAlmacenes = idsAlmacen.size;

        this.loadingMetrics = false;
      },
      error: (err) => {
        console.error('Error al cargar métricas de stock', err);
        this.loadingMetrics = false;
      }
    });

    // Cargar total productos catalogados
    this.productService.listar().subscribe({
      next: (products) => {
        this.totalProductos = products.length;
      },
      error: (err) => console.error(err)
    });
  }

  setTab(tab: 'dashboard' | 'stocks' | 'productos' | 'movimientos'): void {
    this.activeTab = tab;
    if (tab === 'dashboard') {
      this.cargarMetricas();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
