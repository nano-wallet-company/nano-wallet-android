package co.nano.nanowallet.ui.home;

import com.airbnb.epoxy.Typed2EpoxyController;

import java.util.List;

import co.nano.nanowallet.TransactionBindingModel_;
import co.nano.nanowallet.model.Transaction;

/**
 * Controller to handle adding and removing views on the wallet
 */

public class WalletController extends Typed2EpoxyController<List<Transaction>, CurrencyPagerEnum> {

    @Override
    protected void buildModels(List<Transaction> transactions, CurrencyPagerEnum currencyType) {
        for (Transaction transaction: transactions) {
            new TransactionBindingModel_()
                    .id(transaction.hashCode())
                    .transaction(transaction)
                    .addTo(this);
        }
    }
}
