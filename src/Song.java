/**
 * Class to create a Song object to be added into a SongLibrary
 * 
 * @author Nic Falcione
 * @version 10/24/17
 */
public class Song {
    private String name;
    private String artist;
    private String album;
    private int year;

    /**
     * Constructor to create a song object
     * 
     * @param name
     *            name of the song
     * @param artist
     *            artist of the song
     * @param album
     *            album of the song
     * @param year
     *            the year the song was released
     */
    public Song(String name, String artist, String album, int year) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.year = year;
    }

    /**
     * getter for the name field
     * 
     * @return name of the song
     */
    public String getName() {
        return name;
    }

    /**
     * getter for the artist field
     * 
     * @return the artist of the song
     */
    public String getArtist() {
        return artist;
    }

    /**
     * getter for the album field
     * 
     * @return the album of the song
     */
    public String getAlbum() {
        return album;
    }

    /**
     * getter for year of release field
     * 
     * @return the year the song was released
     */
    public int getYear() {
        return year;
    }

}
