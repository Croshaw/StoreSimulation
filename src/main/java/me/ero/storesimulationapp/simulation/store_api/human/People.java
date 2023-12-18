package me.ero.storesimulationapp.simulation.store_api.human;

public class People {
    private String surname;
    private String name;
    private String patronymic;
    public People(String surname, String name, String patronymic) {
        setSurname(surname);
        setName(name);
        setPatronymic(patronymic);
    }
    public void setSurname(String surname) {
        if(surname.isBlank() || surname.isEmpty())
            throw new RuntimeException("Surname must not be empty or blank");
        this.surname = surname;
    }

    public void setName(String name) {
        if(name.isBlank() || name.isEmpty())
            throw new RuntimeException("Name must not be empty or blank");
        this.name = name;
    }

    public void setPatronymic(String patronymic) {
        if(patronymic.isBlank() || patronymic.isEmpty())
            throw new RuntimeException("Patronymic must not be empty or blank");
        this.patronymic = patronymic;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getPatronymic() {
        return patronymic;
    }

    @Override
    public String toString() {
        return surname + " " + name.charAt(0) + ". " + patronymic.charAt(0) + ".";
    }
}
