package com.dgex.offspring.application.parts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nxt.Block;
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
import com.dgex.offspring.nxtCore.core.BlockDB;
import com.dgex.offspring.nxtCore.core.NXTTime;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.swt.table.IPageableStructeredContentProvider;
import com.dgex.offspring.swt.table.Pageable;
import com.dgex.offspring.ui.InspectBlockDialog;
import com.dgex.offspring.user.service.IUserService;

public class RecentBlocksViewer extends GenerericTableViewer {

  static Logger logger = Logger.getLogger(RecentBlocksViewer.class);

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy HH:mm:ss ")
      .provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy H:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Long.valueOf(NXTTime.convertTimestamp(block.getTimestamp()));
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = dateFormat.format(new Date(
              (Long) getCellValue(element)));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnHeight = new GenericTableColumnBuilder(
      "Height").align(SWT.RIGHT).textExtent("#########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getHeight());
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

  final IGenericTableColumn columnID = new GenericTableColumnBuilder("ID")
      .align(SWT.RIGHT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Block block = (Block) element;
          Long id = block.getId();
          if (id != null) {
            InspectBlockDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Long.valueOf(block.getId());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnFee = new GenericTableColumnBuilder("Fee")
      .align(SWT.RIGHT).textExtent("1000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getTotalFee());
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

  final IGenericTableColumn columnAmount = new GenericTableColumnBuilder(
      "Amount").align(SWT.RIGHT).textExtent("#1000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getTotalAmount());
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

  final IGenericTableColumn columnTransactionCount = new GenericTableColumnBuilder(
      "Count").align(SWT.RIGHT).textExtent("##########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getTransactionIds().size());
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

  final IGenericTableColumn columnEmpty = new GenericTableColumnBuilder("")
      .align(SWT.RIGHT).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          return null;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = "";
        }

        @Override
        public int compare(Object v1, Object v2) {
          return 0;
        }
      }).build();

  final IPageableStructeredContentProvider contentProvider = new IPageableStructeredContentProvider() {

    private int currentPage = 1;
    private int pageSize = -1;
    private BlockDB.LazyList list;

    @Override
    public void dispose() {
      if (list != null) {
        list.dispose();
        list = null;
      }
    }

    @Override
    public void reset(Viewer viewer) {
      if (list != null)
        list.dispose();

      if (pageSize < 1)
        throw new RuntimeException("Illegal page size");

      Boolean orderAscending = Boolean.FALSE;
      this.list = BlockDB.getBlocks(orderAscending, nxt);
      this.currentPage = 1;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

    @Override
    public Object[] getElements(Object inputElement) {
      if (list == null) {
        return new Object[0];
      }

      logger.info("getElements page=" + currentPage + " size=" + pageSize);
      list.ensureCapacity(currentPage * pageSize + 1);

      Pageable<Block> pageable = new Pageable<Block>(list.getList(), pageSize);
      pageable.setPage(currentPage);

      List<Block> blocks = pageable.getListForPage();
      logger.info("getElements returns ELEMENTS.size=" + blocks.size());
      return blocks.toArray(new Object[blocks.size()]);
    }

    @Override
    public void setCurrentPage(int currentPage) {
      this.currentPage = currentPage;
    }

    @Override
    public void setPageSize(int pageSize) {
      this.pageSize = pageSize;
    }

    @Override
    public int getElementCount() {
      return list == null ? 0 : list.available();
    }
  };

  public INxtService nxt;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;
  private IContactsService contactsService;

  public RecentBlocksViewer(Composite parent, INxtService nxt,
      IStylingEngine engine, IUserService userService, UISynchronize sync,
      IContactsService contactsService) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return 0; // not used
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return null;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnHeight, columnDate, columnID,
            columnFee, columnAmount, columnTransactionCount, columnEmpty };
      }
    });
    refresh();
  }
}
