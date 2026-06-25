# 📊 Billing App - Indian GST Compliant

A full-featured Android billing/invoicing application similar to Vyapar, built with modern Android technologies.

## ✨ Features

### 📄 Invoicing
- **GST Tax Invoices** with full Indian tax compliance
- CGST + SGST (Intra-state) and IGST (Inter-state) support
- HSN code support for all items
- Multiple invoice types: Tax Invoice, Proforma, Quotation, Delivery Challan, Credit/Debit Notes
- **Amount in words** (Indian format - Lakhs, Crores)
- PDF generation and sharing
- Automatic invoice numbering with customizable prefix

### 📦 Inventory Management
- Product catalog with categories
- Stock tracking with low stock alerts
- HSN code and barcode support
- Multiple units (PCS, KG, LTR, MTR, BOX, etc.)
- GST rate assignment per product
- Stock value calculation

### 👥 Party Management
- Customer and Supplier ledgers
- GSTIN validation
- Receivable/Payable tracking
- Opening balance support
- Search by name, phone, or GSTIN

### 💰 Payment Tracking
- Record payments against invoices
- Multiple payment modes (Cash, UPI, Bank Transfer, Cheque, etc.)
- Partial payment support
- Automatic balance calculation

### 📊 Reports
- **Sales Reports** (Daily, Monthly, Yearly)
- **Profit & Loss** statements
- **GST Summary** (CGST, SGST breakup)
- **Outstanding** receivables tracking
- **Top Selling Products** analysis
- Export reports

### ☁️ Google Drive Sync
- **Automatic backup** of entire database to Google Drive
- **One-tap restore** from Drive backup
- Secure OAuth2 authentication
- Offline-first with cloud sync

### 🏢 Business Profile
- Business name, address, GSTIN, PAN
- Bank details for invoice display
- Customizable terms & conditions
- Invoice prefix settings

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Cloud Sync | Google Drive API |
| Navigation | Navigation Compose |
| PDF | Android PDF Document API |

## 📁 Project Structure

```
app/src/main/java/com/billing/app/
├── BillingApplication.kt        # Application class
├── MainActivity.kt               # Main entry point
├── data/
│   ├── entity/                   # Room entities
│   │   ├── Party.kt             # Customer/Supplier
│   │   ├── Product.kt           # Products with GST
│   │   ├── Invoice.kt           # Invoice header
│   │   ├── InvoiceItem.kt       # Invoice line items
│   │   ├── Payment.kt           # Payment records
│   │   ├── BusinessProfile.kt   # Business settings
│   │   ├── Expense.kt           # Expense tracking
│   │   └── SyncLog.kt           # Sync tracking
│   ├── dao/                      # Data Access Objects
│   ├── database/                 # Room Database
│   └── repository/               # Repositories
├── ui/
│   ├── theme/                    # Material 3 theme
│   ├── navigation/               # Nav graph
│   └── screen/
│       ├── dashboard/            # Home dashboard
│       ├── invoice/              # Invoice CRUD + List
│       ├── inventory/            # Product management
│       ├── party/                # Customer/Supplier
│       ├── reports/              # Business reports
│       └── settings/             # App settings
├── viewmodel/                    # ViewModels
├── sync/googledrive/             # Drive sync
├── pdf/                          # PDF generation
├── util/                         # Utilities
│   ├── GstCalculator.kt         # GST tax engine
│   ├── CurrencyUtils.kt         # INR formatting
│   └── DateUtils.kt             # Date helpers
└── di/                           # Hilt DI modules
```

## 🚀 How to Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Google account (for Drive sync)

### Steps

1. **Open in Android Studio**
   ```
   File → Open → Select the `billing-app` folder
   ```

2. **Sync Gradle**
   Android Studio will prompt to sync. Click "Sync Now"

3. **Configure Google Drive (Optional)**
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Create a new project
   - Enable Google Drive API
   - Create OAuth 2.0 credentials (Android type)
   - Add your app's SHA-1 fingerprint
   - Download `google-services.json` and place in `app/` folder

4. **Build & Run**
   - Connect an Android device or start emulator
   - Click ▶️ Run in Android Studio

### Generate Signed APK
1. Build → Generate Signed Bundle / APK
2. Choose APK
3. Create or select keystore
4. Select release build type
5. Finish

## 📱 Screens

| Screen | Description |
|--------|-------------|
| **Dashboard** | Overview with today's sales, monthly stats, quick actions |
| **Invoice List** | All invoices with search and status filters |
| **Create Invoice** | Full invoice creation with item management |
| **Invoice Detail** | View invoice with items, taxes, payment status |
| **Inventory** | Product list with category filters and stock alerts |
| **Add Product** | Add/edit product with HSN, GST, pricing |
| **Parties** | Customer/Supplier list with receivable/payable |
| **Add Party** | Add/edit party with GSTIN validation |
| **Reports** | Sales, Profit, GST, and Outstanding reports |
| **Settings** | Business profile and Google Drive backup |

## 🇮🇳 GST Compliance

- ✅ GSTIN validation (format + state code)
- ✅ CGST + SGST for intra-state transactions
- ✅ IGST for inter-state transactions
- ✅ Multiple GST rates (0%, 0.25%, 3%, 5%, 12%, 18%, 28%)
- ✅ Cess support
- ✅ HSN code per item
- ✅ Place of Supply
- ✅ Reverse Charge flag
- ✅ Amount in words (Indian numbering)
- ✅ State code mapping (all Indian states/UTs)
- ✅ GST summary for GSTR-1 filing

## 📝 License

This project is provided as-is for educational and commercial use.
