package classes;

public class GameVariables {
    int roundBet;
    int totalBet;
    int readyPlayers;
    int numberPlayers;
    int playerTurn;
    int playersCompeting;
    int currentRound;
    int currentlyCompeting;

    public GameVariables() {
        roundBet = 0;
        totalBet = 0;
        readyPlayers = 0;
        numberPlayers = 0;
        playerTurn = 0;
        playersCompeting = 0;
        currentRound = 0;
        currentlyCompeting = 0;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getRoundBet() {
        return roundBet;
    }

    public void setRoundBet(int roundBet) {
        this.roundBet = roundBet;
    }

    public int getTotalBet() {
        return totalBet;
    }

    public void setTotalBet(int totalBet) {
        this.totalBet = totalBet;
    }

    public int getCurrentlyCompeting() {
        return currentlyCompeting;
    }

    public void setCurrentlyCompeting(int currentlyCompeting) {
        this.currentlyCompeting = currentlyCompeting;
    }

    public int getReadyPlayers() {
        return readyPlayers;
    }

    public void setReadyPlayers(int readyPlayers) {
        this.readyPlayers = readyPlayers;
    }

    public int getNumberPlayers() {
        return numberPlayers;
    }

    public void setNumberPlayers(int numberPlayers) {
        this.numberPlayers = numberPlayers;
    }

    public int getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(int playerTurn) {
        this.playerTurn = playerTurn;
    }

    public int getPlayersCompeting() {
        return playersCompeting;
    }

    public void setPlayersCompeting(int playersCompeting) {
        this.playersCompeting = playersCompeting;
    }


}
