package com.dgex.offspring.ui.controls;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Asset;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.user.service.IUserService;

public class AssetsViewer extends GenerericTableViewer {

  static final String EMPTY_STRING = "";

  static Logger logger = Logger.getLogger(AssetsViewer.class);

  final IGenericTableColumn columnName = new GenericTableColumnBuilder("Name")
      .align(SWT.LEFT).textExtent("#########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getName();
          return EMPTY_STRING;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnBalance = new GenericTableColumnBuilder(
      "Balance").align(SWT.RIGHT).textExtent("00000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          if (account.getAssetBalances() != null)
            return Integer.valueOf(account.getAssetBalances().get(id));
          return Long.valueOf(0l);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Integer
              .toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnQuantity = new GenericTableColumnBuilder(
      "Quantity").align(SWT.RIGHT).textExtent("00000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return Integer.valueOf(asset.getQuantity());
          return Long.valueOf(0l);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Integer
              .toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnIssuer = new GenericTableColumnBuilder(
      "Issuer").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null) {
            Long accountId = asset.getAccountId();
            if (accountId != null) {
              InspectAccountDialog.show(accountId, nxt, engine, userService,
                  sync, contactsService);
            }
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getAccountId();
          return Long.valueOf(0l);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnDescription = new GenericTableColumnBuilder(
      "Description").align(SWT.LEFT)
      .textExtent("#####################################")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getDescription();
          return EMPTY_STRING;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

      logger.info("inputChanged");

      accountId = (Long) newInput;
      if (accountId != null)
        account = Account.getAccount(accountId);
      else
        account = null;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (account == null) {
        return new Object[0];
      }

      logger.info("getElements");

      /*
       * We retrieve our own issued assets from the DB and then ask
       * account.getAssetBalances() for the rest
       */

      // Set<Long> assets = new TreeSet<Long>();

      // int timestamp = 0;
      // Boolean orderAscending = Boolean.FALSE;
      // TransactionType[] recipientTypes = {};
      // TransactionType[] senderTypes = {
      // TransactionType.ColoredCoins.ASSET_ISSUANCE };
      //
      // LazyList list = TransactionDB.getTransactions(accountId,
      // recipientTypes,
      // senderTypes, timestamp, orderAscending, nxt);
      // list.ensureCapacity(list.available());
      //
      // List<Long> elements = new ArrayList<Long>();
      // for (ITransaction transaction : list.getList()) {
      // Transaction t = transaction.getNative();
      // elements.add(t.getId());
      // }
      List<Long> elements = new ArrayList<Long>(account.getAssetBalances()
          .keySet());
      return elements.toArray(new Object[elements.size()]);
    }
  };

  private INxtService nxt;
  private Long accountId;
  private IUserService userService;
  private IContactsService contactsService;
  private UISynchronize sync;
  private Account account;
  private IStylingEngine engine;

  public AssetsViewer(Composite parent, Long accountId, INxtService nxt,
      IUserService userService, IContactsService contactsService,
      UISynchronize sync, IStylingEngine engine) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.accountId = accountId;
    this.userService = userService;
    this.contactsService = contactsService;
    this.sync = sync;
    this.account = Account.getAccount(accountId);
    this.engine = engine;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.DESCENDING;
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return columnName;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnName, columnBalance,
            columnQuantity, columnIssuer, columnDescription };
      }
    });
    setInput(accountId);
  }
}
