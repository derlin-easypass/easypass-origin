package inc;


import java.lang.reflect.Field;

class Node implements java.io.Serializable{
       
    
    public String account;
    public String pseudo;
    public String emailAddress;
    public String pass;
    public String notes;
       
    
    public Node(){
        
    };
    
    public Node(String name, String pseudo, String emailAddress, String pass, String notes){
        this.account = name;
        this.pseudo = pseudo;
        this.emailAddress = emailAddress;
        this.pass = pass;
        this.notes = notes;
        
    }//end constructor

    public String[] getFieldNames(){
        
        Field[] fields = Node.class.getDeclaredFields();
        String[] fieldnames = new String[fields.length];
    
        for(int i = 0; i < fields.length; i++){
            fieldnames[i] = fields[i].getName();
        }
        
        return fieldnames;
        
    }//end getFieldNames

    
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Node :");
        
        for(Field f : this.getClass().getDeclaredFields()){            
            try {
                sb.append("\n" + f.getName() + ": " + f.get(this) );
            } catch (Exception e) {
                continue;
            }
        }//end for
                
        return sb.toString();
        
    }//end toString
    
    
    
    public String getName() {
        return account;
    }


    public void setName(String name) {
        this.account = name;
    }

    public String getPseudo() {
        return pseudo;
    }


    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    
    
    public String getEmailAddress() {
        return emailAddress;
    }


    public void setEmailAdress(String emailAdress) {
        this.emailAddress = emailAdress;
    }


    public String getPass() {
        return pass;
    }


    public void setPass(String pass) {
        this.pass = pass;
    }


    public String getNotes() {
        return notes;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }
           
    
}//end class
