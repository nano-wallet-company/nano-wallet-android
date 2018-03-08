package co.nano.nanowallet.model;

import java.util.Locale;

/**
 * Class to define all of the available currency options
 * This is a custom currency implementation because:
 *  1. Older versions of Android do not have full currency functionality (like display name)
 *  2. We need to limit and customize some of the available currencies
 */

public enum AvailableCurrency {
    AUD("AUD"), BRL("BRL"), CAD("CAD"), CHF("CHF"), CLP("CLP"), CNY("CNY"), CZK("CZK"), DKK("DKK"),
    EUR("EUR"), GBP("GBP"), HKD("HKD"), HUF("HUF"), IDR("IDR"), ILS("ILS"), INR("INR"), JPY("JPY"),
    KRW("KRW"), MXN("MXN"), MYR("MYR"), NOK("NOK"), NZD("NZD"), PHP("PHP"), PKR("PKR"), PLN("PLN"),
    RUB("RUB"), SEK("SEK"), SGD("SGD"), THB("THB"), TRY("TRY"), TWD("TWD"), ZAR("ZAR"), USD("USD");

    private String iso4217Code = "";

    AvailableCurrency(String code) {
        this.iso4217Code = code;
    }

    @Override
    public String toString() {
        return iso4217Code;
    }

    public String getFullDisplayName() {
        return getCurrencySymbol() + " " + getDisplayName();
    }

    public String getDisplayName() {
        switch (iso4217Code) {
            case "AUD":
                return "Australian Dollar";
            case "BRL":
                return "Brazilian Real";
            case "CAD":
                return "Canadian Dollar";
            case "CHF":
                return "Swiss Franc";
            case "CLP":
                return "Chilean Peso";
            case "CNY":
                return "Chinese Yuan";
            case "CZK":
                return "Czech Koruna";
            case "DKK":
                return "Danish Krone";
            case "EUR":
                return "Euro";
            case "GBP":
                return "Great Britain Pound";
            case "HKD":
                return "Hong Kong Dollar";
            case "HUF":
                return "Hungarian Forint";
            case "IDR":
                return "Indonesian Rupiah";
            case "ILS":
                return "Israeli Shekel";
            case "INR":
                return "Indian Rupee";
            case "JPY":
                return "Japanese Yen";
            case "KRW":
                return "South Korean Won";
            case "MXN":
                return "Mexican Peso";
            case "MYR":
                return "Malaysian Ringgit";
            case "NOK":
                return "Norwegian Krone";
            case "NZD":
                return "New Zealand Dollar";
            case "PHP":
                return "Philippine Peso";
            case "PKR":
                return "Pakistani Rupee";
            case "PLN":
                return "Polish Zloty";
            case "RUB":
                return "Russian Ruble";
            case "SEK":
                return "Swedish Krona";
            case "SGD":
                return "Singapore Dollar";
            case "THB":
                return "Thai Baht";
            case "TRY":
                return "Turkish Lira";
            case "TWD":
                return "Taiwan New Dollar";
            case "ZAR":
                return "South African Rand";
            case "USD":
            default:
                return "US Dollar";
        }
    }

    public String getCurrencySymbol() {
        switch (iso4217Code) {
            case "AUD":
                return "$";
            case "BRL":
                return "R$";
            case "CAD":
                return "$";
            case "CHF":
                return "CHF";
            case "CLP":
                return "$";
            case "CNY":
                return "¥";
            case "CZK":
                return "Kč";
            case "DKK":
                return "kr.";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "HKD":
                return "HK$";
            case "HUF":
                return "Ft";
            case "IDR":
                return "Rp";
            case "ILS":
                return "₪";
            case "INR":
                return "₹";
            case "JPY":
                return "¥";
            case "KRW":
                return "₩";
            case "MXN":
                return "$";
            case "MYR":
                return "RM";
            case "NOK":
                return "kr";
            case "NZD":
                return "$";
            case "PHP":
                return "₱";
            case "PKR":
                return "Rs";
            case "PLN":
                return "zł";
            case "RUB":
                return "\u20BD";
            case "SEK":
                return "kr";
            case "SGD":
                return "$";
            case "THB":
                return "THB";
            case "TRY":
                return "₺";
            case "TWD":
                return "NT$";
            case "ZAR":
                return "R$";
            case "USD":
            default:
                return "$";
        }
    }

    public Locale getLocale() {
        switch (iso4217Code) {
            case "AUD":
                return new Locale("en", "US");
            case "BRL":
                return new Locale("en", "BR");
            case "CAD":
                return new Locale("en", "US");
            case "CHF":
                return new Locale("en", "CH");
            case "CLP":
                return new Locale("en", "US");
            case "CNY":
                return new Locale("yue", "CN", "Hans");
            case "CZK":
                return new Locale("cs", "CZ");
            case "DKK":
                return new Locale("en", "DK");
            case "EUR":
                return new Locale("en", "EU");
            case "GBP":
                return new Locale("en", "GB");
            case "HKD":
                return new Locale("zh", "HK", "Hans");
            case "HUF":
                return new Locale("hu", "HU");
            case "IDR":
                return new Locale("id", "ID");
            case "ILS":
                return new Locale("en", "IL");
            case "INR":
                return new Locale("en", "IN");
            case "JPY":
                return new Locale("ja", "JP");
            case "KRW":
                return new Locale("ko", "KR");
            case "MXN":
                return new Locale("es", "MX");
            case "MYR":
                return new Locale("ta", "MY");
            case "NOK":
                return new Locale("nn", "NO");
            case "NZD":
                return new Locale("en", "NZ");
            case "PHP":
                return new Locale("fil", "PH");
            case "PKR":
                return new Locale("en", "MU");
            case "PLN":
                return new Locale("pl", "PL");
            case "RUB":
                return new Locale("ru", "RU");
            case "SEK":
                return new Locale("en", "SE");
            case "SGD":
                return new Locale("en", "US");
            case "THB":
                return new Locale("th", "TH");
            case "TRY":
                return new Locale("tr", "TR");
            case "TWD":
                return new Locale("en", "TW");
            case "ZAR":
                return new Locale("pt", "BR");
            case "USD":
            default:
                return new Locale("en", "US");
        }
    }
}
