import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import edu.cnu.cs.gooey.Gooey;
import edu.cnu.cs.gooey.GooeyDialog;
import edu.cnu.cs.gooey.GooeyFrame;

/**
 * Test class for Song Library program
 * 
 * @author Nic Falcione
 * @version 10/24/17
 */
public class SongLibraryTest {

    public File file;

    /**
     * Makes sure Gooey timeout does not effect tests
     */
    @BeforeClass
    public static void newFrameCreatedBeforeTest() {
        JFrame f = new SongLibrary();
        f.setVisible(true);
        f.dispose();
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Checks for initial conditions and components on opening
     */
    @Test
    public void hasTitleAndComponents() {
        Gooey.capture(new GooeyFrame() {
            @Override
            public void invoke() {
                SongLibrary.main(new String[] {});
            }

            @Override
            public void test(JFrame frame) {
                String title = frame.getTitle();
                assertEquals("SongLibrary", title);

                JMenuBar menubar = Gooey.getMenuBar(frame);
                JMenu program = Gooey.getMenu(menubar, "SongLibrary");
                Gooey.getMenu(program, "About...");
                Gooey.getMenu(program, "Exit");

                JMenu program2 = Gooey.getMenu(menubar, "Table");
                Gooey.getMenu(program2, "New");
                Gooey.getMenu(program2, "Open...");
                Gooey.getMenu(program2, "Save As...");

                JButton add = Gooey.getButton(frame, "Add");
                JButton delete = Gooey.getButton(frame, "Delete");

                assertTrue(add.isEnabled());
                assertFalse(delete.isEnabled());

                JTable table = Gooey.getComponent(frame, JTable.class);
                assertEquals(table.getColumnCount(), 4);
                assertEquals(table.getRowCount(), 0);
                TableModel model = table.getModel();
                assertEquals("Song", model.getColumnName(0));
                assertEquals("Artist", model.getColumnName(1));
                assertEquals("Album", model.getColumnName(2));
                assertEquals("Year", model.getColumnName(3));

                Gooey.getComponent(frame, Box.class);
            }
        });
    }

    /**
     * test to see if closing program works with dialog
     */
    @Test
    public void closeProgram() {
        Gooey.capture(new GooeyFrame() {
            @Override
            public void invoke() {
                SongLibrary.main(new String[] {});
            }

            @Override
            public void test(JFrame frame) {
                JMenuBar menubar = Gooey.getMenuBar(frame);
                JMenu program = Gooey.getMenu(menubar, "SongLibrary");
                JMenuItem exit = Gooey.getMenu(program, "Exit");
                Gooey.capture(new GooeyDialog() {

                    @Override
                    public void invoke() {
                        exit.doClick();
                    }

                    @Override
                    public void test(JDialog dialog) {
                        JButton cancel = Gooey.getButton(dialog, "Cancel");
                        JButton yes = Gooey.getButton(dialog, "Yes");
                        cancel.doClick();
                        assertTrue("JFrame should be displayed",
                                frame.isShowing());
                        exit.doClick();
                        yes.doClick();
                    }

                });
            }
        });
    }

    /**
     * Checks to see if the about dialog is working correctly
     */
    @Test
    public void aboutDialogWorking() {
        Gooey.capture(new GooeyFrame() {

            @Override
            public void invoke() {
                SongLibrary.main(new String[] {});
            }

            @Override
            public void test(JFrame frame) {
                JMenuBar menubar = Gooey.getMenuBar(frame);
                JMenu program = Gooey.getMenu(menubar, "SongLibrary");
                JMenuItem about = Gooey.getMenu(program, "About...");
                Gooey.capture(new GooeyDialog() {

                    @Override
                    public void invoke() {
                        about.doClick();
                    }

                    @Override
                    public void test(JDialog dialog) {
                        JButton ok = Gooey.getButton(dialog, "OK");
                        ok.doClick();
                    }

                });
                assertTrue(frame.isShowing());
            }

        });
    }

    /**
     * checks to see if a file can e read and imported into the library
     */
    @Test
    public void loadFileWithSongs() {
        try {
            file = tempFolder.newFile("songs.txt");
            PrintWriter print = new PrintWriter(file);
            print.print("Badfish,Sublime,Jah Don't Pay the Bills,2001");
            print.close();
        } catch (IOException e) {
        }

        Gooey.capture(new GooeyFrame() {

            @Override
            public void invoke() {
                SongLibrary.main(new String[] {});
            }

            @Override
            public void test(JFrame frame) {
                JMenuBar menubar = Gooey.getMenuBar(frame);
                JTable table = Gooey.getComponent(frame, JTable.class);
                JMenu program = Gooey.getMenu(menubar, "Table");
                JMenuItem open = Gooey.getMenu(program, "Open...");
                Gooey.capture(new GooeyDialog() {

                    @Override
                    public void invoke() {
                        open.doClick();
                    }

                    @Override
                    public void test(JDialog dialog) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    JFileChooser choose = Gooey.getComponent(
                                            dialog, JFileChooser.class);
                                    choose.setSelectedFile(file);
                                    choose.approveSelection();

                                    assertEquals("Badfish",
                                            table.getModel().getValueAt(1, 0));
                                    assertEquals("Sublime",
                                            table.getModel().getValueAt(0, 1));
                                    assertEquals("Jah Don't Pay the Bills",
                                            table.getModel().getValueAt(0, 2));
                                    assertEquals("2001",
                                            table.getModel().getValueAt(0, 3));
                                }
                            });
                        } catch (InvocationTargetException
                                | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }

        });
    }

    /**
     * Makes sure songs can be added to the library
     */
    @Test
    public void addSongsToLibrary() {
        Gooey.capture(new GooeyFrame() {

            @Override
            public void invoke() {
                SongLibrary.main(new String[] {});
            }

            @Override
            public void test(JFrame frame) {
                Box box = Gooey.getComponent(frame, Box.class);
                JButton add = Gooey.getButton(box, "Add");
                add.doClick();

                JTable table = Gooey.getComponent(frame, JTable.class);
                assertFalse(table.getModel().getRowCount() <= 0);
            }
        });
    }

}
