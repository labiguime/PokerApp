package classes;

public class TableCards {
    int tableCard1;
    int tableCard2;
    int tableCard3;
    int tableCard4;
    int tableCard5;
    int state;
    String winningHand;

    public TableCards() {
        tableCard1 = -1;
        tableCard2 = -1;
        tableCard3 = -1;
        tableCard4 = -1;
        tableCard5 = -1;
        state = 0;
        winningHand = "No Pair";
    }

    public String getWinningHand() {
        return winningHand;
    }

    public void setWinningHand(String winningHand) {
        this.winningHand = winningHand;
    }

    public int getTableCard1() {
        return tableCard1;
    }

    public void setTableCard1(int tableCard1) {
        this.tableCard1 = tableCard1;
    }

    public int getTableCard2() {
        return tableCard2;
    }

    public void setTableCard2(int tableCard2) {
        this.tableCard2 = tableCard2;
    }

    public int getTableCard3() {
        return tableCard3;
    }

    public void setTableCard3(int tableCard3) {
        this.tableCard3 = tableCard3;
    }

    public int getTableCard4() {
        return tableCard4;
    }

    public void setTableCard4(int tableCard4) {
        this.tableCard4 = tableCard4;
    }

    public int getTableCard5() {
        return tableCard5;
    }

    public void setTableCard5(int tableCard5) {
        this.tableCard5 = tableCard5;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
