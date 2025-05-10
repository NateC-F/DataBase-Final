public class Food {
    private int id;
    private double cost;
    private String name;

    public Food(int id, double cost, String name)
    {
        setId(id);
        setCost(cost);
        setName(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }
}
