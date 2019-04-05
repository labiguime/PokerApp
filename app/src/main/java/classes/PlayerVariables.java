package classes;

public class PlayerVariables {
    String bestHandName;
    String nickname;
    String avatar;
    int[] cards = new int[2];
    int money;

    public PlayerVariables(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
        bestHandName = "No Pair";
        cards[0] = -1;
        cards[1] = -1;
        money = 10000;
    }

    public void setBestHandName(String bestHandName) {
        this.bestHandName = bestHandName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setCards(int[] cards) {
        this.cards = cards;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getBestHandName() {
        return bestHandName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public int[] getCards() {
        return cards;
    }

    public int getMoney() {
        return money;
    }
}
