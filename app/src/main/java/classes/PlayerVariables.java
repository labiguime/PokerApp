package classes;

public class PlayerVariables {
    String bestHandName;
    String nickname;
    String avatar;
    int card1;
    int card2;
    int money;
    int lastRoundPlayed;
    int currentBet;

    public PlayerVariables() {
        this.nickname = "Nobody";
        this.avatar = "profile_icon";
        bestHandName = "No Pair";
        card1 = -1;
        card2 = -1;
        money = 10000;
        lastRoundPlayed = -1;
    }

    public PlayerVariables(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
        bestHandName = "No Pair";
        card1 = -1;
        card2 = -1;
        money = 10000;
        currentBet = 0;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    public int getLastRoundPlayed() {
        return lastRoundPlayed;
    }

    public void setLastRoundPlayed(int lastRoundPlayed) {
        this.lastRoundPlayed = lastRoundPlayed;
    }

    public int getCard1() {
        return card1;
    }

    public void setCard1(int card1) {
        this.card1 = card1;
    }

    public int getCard2() {
        return card2;
    }

    public void setCard2(int card2) {
        this.card2 = card2;
    }

    public int getMoney() {
        return money;
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
}
