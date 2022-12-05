package com.example.lab.webServlet.Controller;

import com.example.lab.model.Balloon;
import com.example.lab.model.Manufacturer;
import com.example.lab.model.Order;
import com.example.lab.service.BalloonService;
import com.example.lab.service.ManufacturerService;
import com.example.lab.service.OrderService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/balloons")
public class BalloonController {
    private final BalloonService balloonService;
    private final ManufacturerService manufacturerService;
    private  final OrderService orderService;

    public BalloonController(BalloonService balloonService, ManufacturerService manufacturerService, OrderService orderService) {
        this.balloonService = balloonService;
        this.manufacturerService = manufacturerService;
        this.orderService = orderService;
    }

    @GetMapping
    public String getBalloonsPage(@RequestParam(required = false) String error, Model model)
    {
        List<Balloon> balloons = this.balloonService.listAll();
        int counter = balloonService.getCounter();
        model.addAttribute("balloons", balloons);
        model.addAttribute("counter",counter);
        return "listBalloons";
    }
    @GetMapping("/edit-balloon/{id}")
    public String getEditBalloonPage(@PathVariable Long id, Model model) {
        if(this.balloonService.findById(id).isPresent()){
            Balloon balloon = this.balloonService.findById(id).get();
            List<Manufacturer> manufacturers = this.manufacturerService.findAll();
            model.addAttribute("manufacturers", manufacturers);
            model.addAttribute("balloon", balloon);
            return "add-balloon";
        }
        return "redirect:/products?error=ProductNotFound";
    }
    @GetMapping("/add-balloon")
    public String getAddBalloonPage(Model model)
    {
        List<Manufacturer> manufacturers = this.manufacturerService.findAll();
        model.addAttribute("manufacturers", manufacturers);
        return "add-balloon";
    }
    @PostMapping("/add")
    public String saveBalloon(@RequestParam String name,
                              @RequestParam String description,
                              @RequestParam Long manufacturer)
    {
        this.balloonService.save(name,description, manufacturer);
        return "redirect:/balloons";
    }
    @DeleteMapping("/delete/{id}")
    public String deleteBalloon(@PathVariable Long id)
    {
        balloonService.deleteById(id);
        return "redirect:/balloons";
    }
    @PostMapping("/select-date")
    public String selectDate(@RequestParam("dateCreated") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateCreated, HttpServletRequest request) {
        request.getSession().setAttribute("dateCreated", dateCreated);
        return "selectBalloonSize";
    }
    @PostMapping("/selectBalloon")
    public String chosenColor(HttpServletRequest request, Model model)
    {
        String color = request.getParameter("color");
        Order order = new Order(color,"",null);
        request.getSession().setAttribute("order",order);
        return "selectBalloonSize";
    }
    @PostMapping("/selectBalloonSize")
    public String chosenSize(HttpServletRequest request, Model model)
    {
        String size = request.getParameter("size");
        LocalDateTime localDateTime = (LocalDateTime) request.getSession().getAttribute("dateCreated");
        Order order = (Order) request.getSession().getAttribute("order");
        order.setDateCreated(localDateTime);
        orderService.save();
        order.setBalloonSize(size);
        request.getSession().setAttribute("order",order);
        return "deliveryInfo";
    }
    @PostMapping("/deliveryInfo")
    public String deliveryInfo(HttpServletRequest request, Model model)
    {
        String ipAddress = request.getRemoteAddr();
        String clientBrowser = request.getHeader("User-Agent");
        Order order = (Order) request.getSession().getAttribute("order");
        request.getSession().setAttribute("order",order);
        orderService.placeOrder(order.getBalloonColor(),order.getBalloonSize(),order.getDateCreated());
        model.addAttribute("ipAddress",ipAddress);
        model.addAttribute("clientBrowser",clientBrowser);
        return "confirmationInfo";
    }
    @GetMapping("/orders")
    public String getOrdersPage(Model model)
    {
        List<Order> orders = orderService.listAll();
        model.addAttribute("orders",orders);
        return "userOrders";
    }
}
