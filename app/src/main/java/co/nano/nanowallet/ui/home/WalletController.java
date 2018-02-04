package co.nano.nanowallet.ui.home;

import com.airbnb.epoxy.TypedEpoxyController;

import java.util.List;

import co.nano.nanowallet.TransactionBindingModel_;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;

/**
 * Controller to handle adding and removing views on the wallet
 */

public class WalletController extends TypedEpoxyController<List<AccountHistoryResponseItem>> {

    @Override
    protected void buildModels(List<AccountHistoryResponseItem> accountHistoryResponses) {
        for (AccountHistoryResponseItem accountHistoryItem: accountHistoryResponses) {
            new TransactionBindingModel_()
                    .id(accountHistoryItem.getHash())
                    .accountHistoryItem(accountHistoryItem)
                    .addTo(this);
        }
    }
}
