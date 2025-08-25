package Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "UserAccount")
public class UserAccount {
    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @Lob
    @Column(name = "password", nullable = false)
    private String password;

    @ColumnDefault("0")
    @Column(name = "highestCoin", nullable = false)
    private Integer highestCoin = 0;

    @ColumnDefault("0")
    @Column(name = "totalCoin", nullable = false)
    private Integer totalCoin = 0;

    @ColumnDefault("0")
    @Column(name = "winTime", nullable = false)
    private Integer winTime = 0;
    
    public UserAccount() {
    	this.highestCoin = 0;
        this.totalCoin = 0;
        this.winTime = 0;
	}
    public UserAccount(String username, String password, Integer highestCoin, Integer totalCoin, Integer winTime) {
		this.username = username;
		this.password = password;
		this.highestCoin = (highestCoin != null) ? highestCoin : 0;
        this.totalCoin = (totalCoin != null) ? totalCoin : 0;
        this.winTime = (winTime != null) ? winTime : 0;
	}


	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getHighestCoin() {
        return highestCoin;
    }

    public void setHighestCoin(Integer highestCoin) {
        this.highestCoin = highestCoin;
    }

    public Integer getTotalCoin() {
        return totalCoin;
    }

    public void setTotalCoin(Integer totalCoin) {
        this.totalCoin = totalCoin;
    }

    public Integer getWinTime() {
        return winTime;
    }

    public void setWinTime(Integer winTime) {
        this.winTime = winTime;
    }

}