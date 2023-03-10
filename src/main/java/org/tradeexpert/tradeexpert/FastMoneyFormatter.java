package org.tradeexpert.tradeexpert;

import org.jetbrains.annotations.NotNull;

public class FastMoneyFormatter implements MoneyFormatter<Money> {
    @Override
    public String format(Money money) {
        if (money instanceof DefaultMoney) {
            DefaultMoneyFormatter defaultMoneyFormatter;
            if (money.currency().getCurrencyType() == CurrencyType.FIAT) {
                defaultMoneyFormatter = DefaultMoneyFormatter.DEFAULT_FIAT_FORMATTER;
            } else {
                defaultMoneyFormatter = DefaultMoneyFormatter.DEFAULT_CRYPTO_FORMATTER;
            }

            return defaultMoneyFormatter.format((DefaultMoney) money);
        } else if (money instanceof FastMoney) {
            return format((FastMoney) money);
        } else {
            throw new IllegalArgumentException("Unknown money type: " + money.getClass());
        }
    }

    private String format(@NotNull FastMoney money) {
        if (money.getPrecision() == 0) {
            // TODO ideally this would only trigger if the fractional digits of the currency was 0
            // but that is not yet how it's working
            if (money.currency().getCurrencyType() == CurrencyType.FIAT) {
                return money.currency().getSymbol() + money.amount() + ".00";
            } else {
                return Long.toString(money.amount()) + ' ' + money.currency().getCode();
            }
        }

        final char[] buf = new char[FastMoney.Utils.MAX_LONG_LENGTH + 3];
        int p = buf.length;
        int remainingPrecision = money.getPrecision();
        long units = Math.abs(money.amount());
        long q;
        long rem;
        while (remainingPrecision > 0 && units > 0) {
            q = units / 10;
            rem = (int) (units - q * 10);
            buf[--p] = (char) ('0' + rem);
            units = q;
            --remainingPrecision;
        }
        if (units == 0 && remainingPrecision == 0) {
            buf[--p] = '.';
            buf[--p] = '0';
        } else if (units == 0) {
            while (remainingPrecision > 0) {
                buf[--p] = '0';
                --remainingPrecision;
            }
            buf[--p] = '.';
            buf[--p] = '0';
        } else if (remainingPrecision == 0) {
            buf[--p] = '.';
            while (units > 0) {
                q = units / 10;
                rem = (int) (units - q * 10);
                buf[--p] = (char) ('0' + rem);
                units = q;
            }
        }
        if (money.amount() < 0) {
            buf[--p] = '-';
        }

        // add symbol/code depending on type
        if (money.currency().getCurrencyType() == CurrencyType.FIAT) {
            buf[--p] = money.currency().getSymbol().charAt(0);
        }

        // TODO make this fast like the rest of the code and work for the general case of fractionalDigits
        // instead of mandating only 2 digits
        String result = new String(buf, p, buf.length - p);
        if (result.charAt(result.length() - 2) == '.') {
            result += '0';
        }

        if (money.currency().getCurrencyType() == CurrencyType.CRYPTO) {
            result += ' ' + money.currency().getCode();
        }
        return result;
    }

}
