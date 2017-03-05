package net.slm.batch;

public class Person {
    private String lastName;
    private String firstName;
    private String age;

    public Person() {

    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public Person(String firstName,  String lastName , String age){
    	this.firstName = firstName;
        this.lastName = lastName;
        this.age =  age;
    }
    

    public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "firstName: " + firstName + ", lastName: " + lastName +", age: "+age;
    }

}
