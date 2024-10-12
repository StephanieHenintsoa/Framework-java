package verb;
public class VerbAction {
    public String verbe;
    public String methode;

    public VerbAction(String verbe, String methode) {
        this.verbe = verbe;
        this.methode = methode;
    }

    public String getVerbe() {
        return verbe;
    }

    public void setVerbe(String verbe) {
        this.verbe = verbe;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }

    // Méthode toString pour afficher les détails
    @Override
    public String toString() {
        return "VerbAction { " +
                "verbe='" + verbe + '\'' +
                ", methode='" + methode + '\'' +
                " }";
    }
}
