import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Class to create song library with name, artist, album, and year of release
 * 
 * @author Nic Falcione
 * @version 10/24/17
 */
@SuppressWarnings("serial")
public class SongLibrary extends JFrame {

    private ArrayList<Song> library;

    /**
     * Constructor to create a Song Library Program frame
     */
    public SongLibrary() {
        setTitle("SongLibrary");
        setLayout(new BorderLayout());

        String[] columns = { "Song", "Artist", "Album", "Year" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(500, 100));
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table));

        Box box = Box.createVerticalBox();
        add(box, BorderLayout.EAST);

        JMenuItem about = new JMenuItem("About...");
        JMenuItem exit = new JMenuItem("Exit");

        JMenuItem newTable = new JMenuItem("New");
        JMenuItem open = new JMenuItem("Open...");
        JMenuItem save = new JMenuItem("Save As...");

        JMenu libMenu = new JMenu("SongLibrary");
        JMenu tableMenu = new JMenu("Table");

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        menubar.add(libMenu);
        menubar.add(tableMenu);

        libMenu.add(about);
        libMenu.add(exit);

        tableMenu.add(newTable);
        tableMenu.add(open);
        tableMenu.add(save);

        JButton add = new JButton("Add");
        JButton delete = new JButton("Delete");
        delete.setEnabled(false);
        box.add(add, BorderLayout.EAST);
        box.add(delete, BorderLayout.EAST);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                Object[] options = { "Yes", "No", "Cancel" };
                int action = JOptionPane.showOptionDialog(null,
                        "Do you want to exit?", "Select an Option",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, null);
                if (action == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new Object[] { "", "", "", "" });
                delete.setEnabled(true);
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int numRows = table.getSelectedRows().length;

                if (numRows == 0) {
                    JOptionPane.showMessageDialog(null, "No row selected");
                }

                for (int i = 0; i < numRows; i++) {
                    model.removeRow(table.getSelectedRow());
                }

                if (isTableEmpty(table)) {
                    delete.setEnabled(false);
                }
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame f = new JFrame();
                JOptionPane.showMessageDialog(f,
                        "-------------------------\nSongLibrary\n"
                                + "by Nic O. Falcione\n-------------------------");
            }
        });

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = { "Yes", "No", "Cancel" };
                int action = JOptionPane.showOptionDialog(null,
                        "Do you want to exit?", "Select an Option",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, null);
                if (action == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        newTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = { "Yes", "No", "Cancel" };
                int action = JOptionPane.showOptionDialog(null,
                        "Clear all table data?", "Select an Option",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, null);
                if (action == JOptionPane.YES_OPTION) {
                    model.setRowCount(0);
                    delete.setEnabled(false);
                }
            }
        });

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser browse = new JFileChooser();
                int result = browse.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = browse.getSelectedFile();
                    setTitle("SongLibrary [" + file.getAbsolutePath() + ']');
                    getSongsFromFile(file);
                    ArrayList<Song> copy = getLibrary();

                    for (int i = 0; i < copy.size(); i++) {
                        model.addRow(new Object[] { copy.get(i).getName(),
                                copy.get(i).getArtist(), copy.get(i).getAlbum(),
                                Integer.toString(copy.get(i).getYear()) });
                    }
                    if (!isTableEmpty(table)) {
                        delete.setEnabled(true);
                    }
                    clear();
                }
            }
        });

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFileChooser browse = new JFileChooser();
                        int result = browse.showSaveDialog(SongLibrary.this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File file = browse.getSelectedFile();
                            setTitle("SongLibrary [" + file.getAbsolutePath()
                                    + ']');
                            saveTableToFile(table, file);
                        }
                    }
                });
            }
        });

        pack();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Method to grab songs from txt file
     * 
     * @param file
     *            CSV file to be parsed
     */
    protected void getSongsFromFile(File file) {
        try {
            library = new ArrayList<Song>();
            Scanner scan = new Scanner(file);
            Scanner csvscan;

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                csvscan = new Scanner(line);
                csvscan.useDelimiter(",");

                String name = csvscan.next();
                String artist = csvscan.next();
                String album = csvscan.next();
                int year = Integer.parseInt(csvscan.next());
                library.add(new Song(name, artist, album, year));
                csvscan.close();
            }

            scan.close();
        } catch (IOException e) {

        }
    }

    /**
     * converts table into a file with csv
     * 
     * @param table
     *            table to get contents from
     * @return file to be saved
     */
    protected void saveTableToFile(JTable table, File file) {
        try {
            PrintWriter print = new PrintWriter(file);

            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < 4; j++) {
                    if (j == 3) {
                        print.print(table.getModel().getValueAt(i, j));
                    } else {
                        print.print(table.getModel().getValueAt(i, j) + ",");
                    }
                }
                print.println();
            }
            print.close();
        } catch (IOException e) {

        }
    }

    /**
     * getter for the library of song objects
     * 
     * @return arraylist of songs
     */
    protected ArrayList<Song> getLibrary() {
        return library;
    }

    /**
     * Clears arraylist to not readd songs
     */
    protected void clear() {
        library.clear();
    }

    /**
     * Checks if the program's table is empty
     * 
     * @param table
     *            JTable object to check
     * @return whether table is empty or not
     */
    protected boolean isTableEmpty(JTable table) {
        if (table != null && table.getModel() != null) {
            return table.getModel().getRowCount() <= 0 ? true : false;
        }
        return false;
    }

    /**
     * Main method
     * 
     * @param args
     *            Command Line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new SongLibrary();
                f.setVisible(true);
            }
        });
    }
}
