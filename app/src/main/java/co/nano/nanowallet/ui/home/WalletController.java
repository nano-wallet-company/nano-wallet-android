package co.nano.nanowallet.ui.home;

import com.airbnb.epoxy.Typed2EpoxyController;

import java.util.List;

import co.nano.nanowallet.TransactionBindingModel_;
import co.nano.nanowallet.network.model.response.AccountHistoryResponseItem;

/**
 * Controller to handle adding and removing views on the wallet
 */

public class WalletController extends Typed2EpoxyController<List<AccountHistoryResponseItem>, HomeFragment.ClickHandlers> {

    @Override
    protected void buildModels(List<AccountHistoryResponseItem> accountHistoryResponses, HomeFragment.ClickHandlers clickHandlers) {
        for (AccountHistoryResponseItem accountHistoryItem: accountHistoryResponses) {
            new TransactionBindingModel_()
                    .id(accountHistoryItem.getHash())
                    .accountHistoryItem(accountHistoryItem)
                    .handlers(clickHandlers)
                    .addTo(this);
        }
    }
}
