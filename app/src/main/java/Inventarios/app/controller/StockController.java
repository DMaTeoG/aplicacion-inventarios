package Inventarios.app.controller;

import Inventarios.app.model.Stock;
import Inventarios.app.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockRepository stockRepository;

    /**
     * Devuelve las existencias físicas de inventario (cantidad de cada producto en cada almacén).
     */
    @GetMapping
    public List<Stock> listarExistencias() {
        return stockRepository.findAll();
    }
}
