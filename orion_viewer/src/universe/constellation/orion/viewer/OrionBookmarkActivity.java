/*
 * Orion Viewer - pdf, djvu, xps and cbz file viewer for android devices
 *
 * Copyright (C) 2011-2012  Michael Bogdanov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package universe.constellation.orion.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import universe.constellation.orion.viewer.bookmarks.*;

import java.io.*;
import java.util.*;

/**
 * User: mike
 * Date: 25.02.12
 * Time: 16:20
 */
public class OrionBookmarkActivity extends OrionBaseActivity {

    public static final String OPEN_PAGE = "open_page";

    public static final String OPENED_FILE = "opened_file";

    public static final String BOOK_ID = "book_id";

    public static final String NAMESPACE = "";

    public static final int IMPORT_CURRRENT = 1;

    public static final int IMPORT_ALL = 2;

    private long bookId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(Device.Info.NOOK_CLASSIC ? R.layout.nook_bookmarks : R.layout.bookmarks);

        onNewIntent(getIntent());

        ListView view = (ListView) findMyViewById(R.id.bookmarks);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bookmark bookmark = (Bookmark) parent.getItemAtPosition(position);
                Intent result = new Intent();
                result.putExtra(OPEN_PAGE, bookmark.page);
                System.out.println("bookmark id = " + bookmark.id + " page = " + bookmark.page);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        ImageButton menu = (ImageButton) findMyViewById(R.id.nook_bookmarks_menu);
        if (menu != null) {
            menu.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    openOptionsMenu();
                }
            });
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bookId = intent.getLongExtra(BOOK_ID, -1);
        updateView(bookId);
    }

    private void updateView(long bookId) {
        BookmarkAccessor accessor = getOrionContext().getBookmarkAccessor();
        List bookmarks = accessor.selectBookmarks(bookId);
        ListView view = (ListView) findMyViewById(R.id.bookmarks);
        view.setAdapter(new ArrayAdapter(this, R.layout.bookmark_entry, R.id.bookmark_entry, bookmarks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = super.getView(position, convertView, parent);
                Bookmark item = (Bookmark) getItem(position);
                TextView page = (TextView) convertView.findViewById(R.id.bookmark_entry_page);
                page.setText("" + (item.page == - 1 ? "*" : item.page + 1));
                return convertView;
            }
        });
    }

    @Override
    public boolean supportDevice() {
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        if (result) {
            getMenuInflater().inflate(R.menu.bookmarks_menu, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean showEmptyResult = false;

        switch (item.getItemId()) {

            case R.id.close_bookmarks_menu_item:
                finish();
                return true;

            case R.id.export_bookmarks_menu_item:
                if (bookId == -1) {
                    showEmptyResult = true;
                }

            case R.id.export_all_bookmarks_menu_item:
                String file = getOrionContext().getTempOptions().openedFile;
                if (file == null) {
                    showEmptyResult = true;
                }

                if (!showEmptyResult) {
                    long bookId = item.getItemId() == R.id.export_all_bookmarks_menu_item ? -1 : this.bookId;
                    file = file + "." + (bookId == -1 ? "all_" : "") +  "bookmarks.xml";
                    Common.d("Bookmarks output file: " + file);

                    BookmarkExporter exporter = new BookmarkExporter(getOrionContext().getBookmarkAccessor(), file);
                    try {
                        showEmptyResult = !exporter.export(bookId);
                    } catch (IOException e) {
                        showError(e);
                        return true;
                    }
                }

                if (showEmptyResult) {
                    showLongMessage("There is nothing to export!");
                } else {
                    showLongMessage("Bookmarks exported to " + file);
                }
                return true;

            case R.id.import_current_bookmarks_menu_item:
                Intent intent = new Intent(this, OrionFileSelectorActivity.class);
                startActivityForResult(intent, IMPORT_CURRRENT);
                return true;

            case R.id.import_all_bookmarks_menu_item:
                intent = new Intent(this, OrionFileSelectorActivity.class);
                startActivityForResult(intent, IMPORT_ALL);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final String fileName = data.getStringExtra(OrionFileSelectorActivity.RESULT_FILE_NAME);
            if (fileName == null || "".equals(fileName)) {
                showWarning("File name is empty");
                return;
            } else {
                Common.d("To import " + fileName);
                List<BookNameAndSize> books = listBooks(fileName);
                if (books == null) {
                    return;
                }

                if (books.isEmpty()) {
                    showWarning("There is no any bookmarks");
                }

                if (IMPORT_CURRRENT == requestCode) {
                    Collections.sort(books);

                    View group = getLayoutInflater().inflate(R.layout.bookmark_book_list, null);
                    final ListView tree = (ListView) group.findViewById(R.id.book_list);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select source book").setCancelable(true).setView(group);
                    builder.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            doImport(fileName, (BookNameAndSize) (tree.getAdapter().getItem(tree.getCheckedItemPosition())));
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


                    final AlertDialog dialog = builder.create();


                    tree.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    tree.setItemsCanFocus(false);
                    tree.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    });
                    tree.setAdapter(new ArrayAdapter<BookNameAndSize>(this, R.layout.select_book_item, R.id.title, books) {

                        private boolean positiveDisabled;
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            convertView = super.getView(position, convertView, parent);

                            BookNameAndSize book = getItem(position);
                            TextView view = (TextView) convertView.findViewById(R.id.page);
                            view.setText(book.buityfySize());

                            view = (TextView) convertView.findViewById(R.id.title);
                            view.setText(book.getName());

                            if (!positiveDisabled) {
                                //android bug
                                positiveDisabled = true;
                                ((AlertDialog)dialog).getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                            }

                            return convertView;
                        }
                    });
                    tree.setItemsCanFocus(false);

                    dialog.show();
                } else {
//                    if (IMPORT_CURRRENT == requestCode) {
//                        doImport(books.get(0));
//                    } else {
                        //all
                        doImport(fileName, null);
//                    }
                }
            }
        }
    }

    private void doImport(String fileName, BookNameAndSize book){
        if (book != null) {
            Common.d("Import d " + book.getName());
        } else {
            Common.d("Import all bookmarks");
        }

        BookmarkImporter importer = new BookmarkImporter(this, getOrionContext().getBookmarkAccessor(), fileName, book);
        try {
            importer.doImport();
            updateView(getOrionContext().getBookmarkAccessor().selectBookId(getOrionContext().getCurrentBookParameters().simpleFileName, getOrionContext().getCurrentBookParameters().fileSize));
            showFastMessage("Imported successfully");
        } catch (OrionException e) {
            showAlert("Error", e.getMessage());
        }

    }


    public List<BookNameAndSize> listBooks(String fileName) {
        ArrayList<BookNameAndSize> bookNames = new ArrayList<BookNameAndSize>();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(new File(fileName)));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);

            int eventType = xpp.getEventType();
            boolean wasBookmarks = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    if ("bookmarks".equals(tagName)) {
                        wasBookmarks = true;
                    } else if (wasBookmarks) {
                        if ("book".equals(tagName)) {
                            String name = xpp.getAttributeValue(NAMESPACE, "fileName");
                            long size = Long.valueOf(xpp.getAttributeValue(NAMESPACE, "fileSize"));
                            BookNameAndSize book = new BookNameAndSize(name, size);
                            bookNames.add(book);
                        }
                    }
                }
                eventType = xpp.next();
            }
            return bookNames;
        } catch (FileNotFoundException e) {
            showError("Couldn't open file", e);
        } catch (XmlPullParserException e) {
            showError("Couldn't parse book parameters", e);
        } catch (IOException e) {
            showError("Couldn't parse book parameters", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Common.d(e);
                }
            }
        }
        return null;
    }

}
