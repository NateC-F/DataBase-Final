public class Book {
    private String name;
    private String genres;
    private int copiesLeft;

    public Book(String name, String genres, int copiesLeft)
    {
        setName(name);
        setGenres(genres);
        setCopiesLeft(copiesLeft);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public int getCopiesLeft() {
        return copiesLeft;
    }

    public void setCopiesLeft(int copiesLeft) {
        this.copiesLeft = copiesLeft;
    }
}
