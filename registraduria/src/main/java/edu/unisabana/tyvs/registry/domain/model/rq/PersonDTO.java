package edu.unisabana.tyvs.registry.domain.model.rq;

import javax.validation.constraints.NotBlank;

public class PersonDTO {
    @NotBlank(message = "name es obligatorio")
    private String name;
    private int id;
    private int age;
    @NotBlank(message = "gender es obligatorio")
    private String gender;
    private boolean alive;

    public PersonDTO() {
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
