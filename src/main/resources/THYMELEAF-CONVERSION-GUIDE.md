# Thymeleaf Conversion Guide for Pharmacy Management UI

## Quick Answer
**You need to copy: `/pharmacy-styles-for-thymeleaf.css`**

This file contains all the CSS styles needed to maintain the exact design when converting to Thymeleaf.

---

## Setup Instructions

### Option 1: Use Tailwind CDN (Recommended - Easiest)

Add this to your Thymeleaf layout/header:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PharmaMed</title>
    
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body>
    <!-- Your content -->
</body>
</html>
```

**Benefit**: You can use the EXACT same class names from the React components!

---

### Option 2: Use Custom CSS File

1. **Copy the file** `/pharmacy-styles-for-thymeleaf.css` to your Spring Boot project:
   ```
   src/main/resources/static/css/pharmacy-styles.css
   ```

2. **Include it in your Thymeleaf template**:
   ```html
   <!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org">
   <head>
       <meta charset="UTF-8">
       <meta name="viewport" content="width=device-width, initial-scale=1.0">
       <title>PharmaMed</title>
       
       <!-- Custom Pharmacy Styles -->
       <link rel="stylesheet" th:href="@{/css/pharmacy-styles.css}">
   </head>
   <body>
       <!-- Your content -->
   </body>
   </html>
   ```

---

## Class Name Mapping

The CSS classes in the React components (className) directly map to standard CSS classes:

### React Component:
```jsx
<div className="bg-white rounded-xl border border-gray-200 p-6">
```

### Thymeleaf Template:
```html
<div class="bg-white rounded-xl border border-gray-200 p-6">
```

**It's the same!** Just replace `className` with `class`.

---

## Converting React Components to Thymeleaf

### Example: Sales List Table

#### React (SalesList.tsx):
```jsx
<table className="w-full">
  <thead className="bg-gray-50 border-b border-gray-200">
    <tr>
      <th className="text-left py-3 px-4 text-sm font-semibold text-gray-700">
        Sale #
      </th>
    </tr>
  </thead>
  <tbody>
    {filteredSales.map((sale) => (
      <tr key={sale.id} className="border-b border-gray-100 hover:bg-gray-50">
        <td className="py-3 px-4">
          <a href={`/sales/${sale.id}`}>{sale.saleNumber}</a>
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

#### Thymeleaf:
```html
<table class="w-full">
  <thead class="bg-gray-50 border-b border-gray-200">
    <tr>
      <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">
        Sale #
      </th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="sale : ${sales}" 
        class="border-b border-gray-100 hover:bg-gray-50 transition-colors">
      <td class="py-3 px-4">
        <a th:href="@{/sales/{id}(id=${sale.id})}" 
           class="text-sm font-medium text-blue-600 hover:text-blue-700"
           th:text="${sale.saleNumber}">SAL-2026-001</a>
      </td>
    </tr>
  </tbody>
</table>
```

---

## Icon Conversion

The React components use Lucide React icons. For Thymeleaf, you have options:

### Option 1: Use SVG Icons Directly
```html
<!-- Search Icon -->
<svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
        d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
</svg>
```

### Option 2: Use Font Awesome
Add to your template head:
```html
<link rel="stylesheet" 
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
```

Then use:
```html
<i class="fas fa-search w-5 h-5"></i>
```

### Option 3: Use Lucide Icons CDN
```html
<script src="https://unpkg.com/lucide@latest"></script>
<script>
  lucide.createIcons();
</script>

<!-- Then use -->
<i data-lucide="search" class="w-5 h-5"></i>
```

---

## Status Badge Helper (Thymeleaf Fragment)

Create a reusable fragment for status badges:

**File: `fragments/status-badge.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

<th:block th:fragment="status-badge(status)">
  <span th:class="'badge badge-' + ${#strings.toLowerCase(status)}"
        class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border">
    <span th:text="${status}">PAID</span>
  </span>
</th:block>

</body>
</html>
```

**Usage:**
```html
<div th:replace="~{fragments/status-badge :: status-badge(${sale.status})}"></div>
```

---

## Complete Thymeleaf Example: Sales List Page

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layouts/main}">
<head>
    <title>Sales - PharmaMed</title>
</head>
<body>

<div layout:fragment="content" class="space-y-6">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
        <div>
            <h1 class="text-3xl font-bold text-gray-900">Sales</h1>
            <p class="mt-2 text-gray-600">View and manage all sales transactions</p>
        </div>
        <a th:href="@{/pos}" 
           class="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            New Sale
        </a>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white rounded-xl border border-gray-200 p-6">
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-sm text-gray-600">Total Sales</p>
                    <p class="text-2xl font-bold text-gray-900 mt-1">
                        $<span th:text="${#numbers.formatDecimal(totalSales, 1, 2)}">43,278.47</span>
                    </p>
                </div>
                <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
            </div>
        </div>
        <!-- Repeat for other stats -->
    </div>

    <!-- Sales Table -->
    <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Sale #</th>
                        <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Customer</th>
                        <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Date</th>
                        <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Total</th>
                        <th class="text-left py-3 px-4 text-sm font-semibold text-gray-700">Status</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="sale : ${sales}" 
                        class="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                        <td class="py-3 px-4">
                            <a th:href="@{/sales/{id}(id=${sale.id})}" 
                               class="text-sm font-medium text-blue-600 hover:text-blue-700"
                               th:text="${sale.saleNumber}">SAL-2026-001</a>
                        </td>
                        <td class="py-3 px-4">
                            <div class="text-sm">
                                <div class="font-medium text-gray-900" 
                                     th:text="${sale.customerName ?: 'Walk-in'}">John Doe</div>
                                <div th:if="${sale.customerPhone}" 
                                     class="text-gray-500" 
                                     th:text="${sale.customerPhone}">+1 234 567 8900</div>
                            </div>
                        </td>
                        <td class="py-3 px-4 text-sm text-gray-600" 
                            th:text="${#temporals.format(sale.saleDate, 'MMM dd, yyyy HH:mm')}">
                            Mar 01, 2026 10:30
                        </td>
                        <td class="py-3 px-4 text-sm font-medium text-gray-900">
                            $<span th:text="${#numbers.formatDecimal(sale.totalAmount, 1, 2)}">132.55</span>
                        </td>
                        <td class="py-3 px-4">
                            <span th:class="'badge badge-' + ${#strings.toLowerCase(sale.status)}"
                                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border">
                                <span th:text="${sale.status}">PAID</span>
                            </span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

</body>
</html>
```
