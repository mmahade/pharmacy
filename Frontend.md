# Pharmacy Management System - Thymeleaf Templates

## Overview
This is a modern pharmacy management system dashboard built with Spring Boot and Thymeleaf. The templates are fully integrated with your existing backend service.

## File Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/pharmacy/
│   │       ├── controller/
│   │       │   └── DashboardController.java
│   │       ├── service/
│   │       │   └── DashboardService.java (your existing service)
│   │       └── dto/
│   │           └── DashboardSummaryResponse.java (your existing DTO)
│   └── resources/
│       ├── templates/
│       │   ├── layout.html (Base layout template)
│       │   ├── dashboard.html (Standalone dashboard)
│       │   └── dashboard-with-layout.html (Dashboard using layout)
│       ├── static/
│       │   ├── css/
│       │   └── js/
│       └── application.properties
```

## Features

### 1. **Modern Dashboard Layout**
- Responsive sidebar navigation
- Search functionality
- User profile dropdown
- Notification bell with badge counter

### 2. **Summary Cards**
- Total Medicines with trend indicator
- Total Prescriptions with trend indicator
- Today's Revenue with trend indicator
- Low Stock Alerts

### 3. **Quick Actions**
- New Sale
- Add Medicine
- New Prescription
- Stock Entry
- Add Customer

### 4. **Charts & Analytics**
- Revenue trend chart (Chart.js line chart)
- Stock overview pie chart (in stock, low stock, out of stock)

### 5. **Alerts**
- Low Stock Alerts with visual indicators
- Expiring Soon items with color-coded severity (critical, warning, notice)

### 6. **Recent Activity Tables**
- Recent Prescriptions with status badges
- Recent Sales with payment method badges

## Controller Setup

The `DashboardController.java` connects your existing service to the Thymeleaf template:

```java
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        DashboardSummaryResponse summary = dashboardService.summary(principal);
        model.addAttribute("summary", summary);
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
```

## Template Options

### Option 1: Standalone Dashboard (`dashboard.html`)
- Complete HTML file with everything included
- No layout inheritance
- Use when you want a self-contained page

### Option 2: Layout-Based Dashboard (`dashboard-with-layout.html`)
- Uses Thymeleaf Layout Dialect
- Inherits from `layout.html`
- Better for multiple pages (DRY principle)
- Recommended for production

## Dependencies Required

Add these to your `pom.xml` if using Maven:

```xml
<dependencies>
    <!-- Spring Boot Starter Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <!-- Thymeleaf Layout Dialect (for layout.html) -->
    <dependency>
        <groupId>nz.net.ultraq.thymeleaf</groupId>
        <artifactId>thymeleaf-layout-dialect</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

Or for Gradle:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
}
```

## Thymeleaf Expressions Used

### Data Binding
```html
<!-- Simple text -->
<span th:text="${summary.totalMedicines}">342</span>

<!-- Formatted decimal -->
<span th:text="${#numbers.formatDecimal(summary.todayRevenue, 1, 2)}">1,847.50</span>

<!-- Date formatting -->
<span th:text="${#temporals.format(prescription.prescriptionDate, 'MMM dd, yyyy')}">Feb 26, 2026</span>

<!-- Conditional rendering -->
<div th:if="${summary.lowStockCount > 0}">Low stock alert!</div>

<!-- Iteration -->
<tr th:each="item : ${summary.lowStock}">
    <td th:text="${item.name}">Medicine Name</td>
</tr>

<!-- Conditional classes -->
<span th:classappend="${item.daysUntilExpiry <= 7} ? 'critical' : 'warning'">
```

### URLs
```html
<!-- Static URLs -->
<a th:href="@{/medicines}">Medicines</a>

<!-- Logout form -->
<form th:action="@{/logout}" method="post">
    <button type="submit">Log out</button>
</form>
```

### Security Integration
```html
<!-- Current user name -->
<p th:text="${#authentication.name}">Admin User</p>

<!-- Check user role -->
<div th:if="${#authorization.expression('hasRole(''ADMIN'')')}">
    Admin content
</div>
```

## CSS & JavaScript Libraries

The templates use CDN links for:
- **Tailwind CSS 3.x** - Utility-first CSS framework
- **Chart.js 4.x** - Charts and graphs
- **Lucide Icons** - Modern icon library

### Customization

To customize colors, modify the gradient classes in `<style>`:

```css
.gradient-blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); }
.gradient-purple { background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%); }
.gradient-green { background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%); }
.gradient-orange { background: linear-gradient(135deg, #f97316 0%, #ea580c 100%); }
```

## Data Model Expected

Your `DashboardSummaryResponse` should match:

```java
public class DashboardSummaryResponse {
    private long totalMedicines;
    private long prescriptionsCount;
    private BigDecimal todayRevenue;
    private int lowStockCount;
    private List<LowStockItem> lowStock;
    private List<ExpiryAlertItem> expiringSoon;
    private List<PrescriptionResponse> recentPrescriptions;
    private List<SaleResponse> recentSales;
    
    // Inner classes
    public record LowStockItem(String name, int stock, int minStock) {}
    public record ExpiryAlertItem(String medicineName, Long medicineId, String batchNumber, 
                                   LocalDate expiryDate, int quantity, int daysUntilExpiry) {}
}
```

## Navigation Structure

The sidebar includes links to:
- Dashboard (`/dashboard`)
- Medicines (`/medicines`)
- Prescriptions (`/prescriptions`)
- Sales (`/sales`)
- Inventory (`/inventory`)
- Customers (`/customers`)
- Reports (`/reports`)
- Alerts (`/alerts`)
- Settings (`/settings`)

Create controllers for each of these sections following the same pattern as `DashboardController`.

## Running the Application

1. Place templates in `src/main/resources/templates/`
2. Configure `application.properties`
3. Run your Spring Boot application
4. Navigate to `http://localhost:8080/`
5. You'll be redirected to `/dashboard`

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- Mobile browsers

## Performance Notes

- Icons are initialized with `lucide.createIcons()` on page load
- Charts are rendered client-side with Chart.js
- Tailwind CSS is loaded via CDN (consider building for production)
- All templates are server-side rendered by Thymeleaf

## Security Considerations

- CSRF protection is enabled by Spring Security
- Logout requires POST request
- User authentication is handled by Spring Security
- Use `@PreAuthorize` annotations for role-based access

## Next Steps

1. Create additional page templates (medicines, prescriptions, sales, etc.)
2. Add form validation
3. Implement AJAX for dynamic updates
4. Add export functionality (PDF, Excel)
5. Create print stylesheets for reports
6. Add dark mode toggle
7. Implement real-time notifications with WebSocket

## License

This template is provided as-is for your pharmacy management system project.
