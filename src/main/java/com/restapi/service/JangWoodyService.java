package com.restapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.restapi.service.holiday.JsonParserHoliday;

@Service
public class JangWoodyService {
	private static final Logger log = LoggerFactory.getLogger(JangWoodyService.class);
	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter DATE_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter DATE_DASHED = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_HHMM = DateTimeFormatter.ofPattern("HHmm");
	private static final DateTimeFormatter TIME_HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String HOLIDAY_YES = "Y";
	private static final String HOLIDAY_NO = "N";
	private static final String[] SKY_LABELS = { "맑음", "구름 조금", "구름 많음", "흐림" };
	private static final String[] PTY_LABELS = { "비나 눈 예보는 없습니다.", "비가 내릴 예정입니다.", "비나 눈이 내릴 예정입니다.", "눈이 내릴 예정입니다.",
			"소나기가 내릴 예정입니다." };
	private static final String UMBRELLA_NOTICE = "\n우산 챙기는걸 잊지 마세요.";
	private static final int WEATHER_DATA_ROW_COUNT = 300;
	private static final int WEATHER_API_RETRY_LIMIT = 5;

	public String getHolidayInfo() {
		String holiday = HOLIDAY_NO;
		LocalDate now = LocalDate.now();
		String formatedNow = now.format(DATE_COMPACT);
		String encodedServiceKey = "tOOuqT4L3Dil4jLLct1gYYqmTI%2BMdLuXcBy9KBp2nkxF2DX2bLg40SVhoU106AYwAoP3CZWdqlSxRp6yG1GKLw%3D%3D";
		String apiUrl = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey="
				+ encodedServiceKey + "&_type=json&numOfRows=20&solYear=" + formatedNow.substring(0, 4) + "&solMonth="
				+ formatedNow.substring(4, 6);
		try {
			log.info("Try to get holiday info : {}", apiUrl);
			RestTemplate restTemplate = new RestTemplateBuilder()
					.defaultHeader(HttpHeaders.USER_AGENT,
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
									+ "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
					.defaultHeader(HttpHeaders.ACCEPT, "*/*").build();
			URI uri = URI.create(apiUrl);
			String json = restTemplate.getForObject(uri, String.class);
			ArrayList<String> holidayArr = JsonParserHoliday.parseHolidayJson(json);
			if (holidayArr != null && holidayArr.contains(formatedNow)) {
				holiday = HOLIDAY_YES;
			}
		} catch (Exception e) {
			log.error("Failed to load holiday information", e);
		}
		int day = now.getDayOfWeek().getValue();
		if (day > 5) {
			holiday = HOLIDAY_YES;
		}

		return holiday;
	}

	public String getWeather(String userKey, String callIP, String callTime, String coX, String coY, String locate) {
		String result = "";

		LocalDate now = LocalDate.now();
		String formatedNow = now.format(DATE_COMPACT);

		log.info(
				"Weather request received [date: {}, userKey: {}, callIP: {}, callTime: {}, coX: {}, coY: {}, locate: {}]",
				formatedNow, userKey, callIP, callTime, coX, coY, locate);

		String apiUrl = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
		String today = resolveBaseDate();

//              apiUrl += "?serviceKey=" + userKey + "&numOfRows=300&dataType=xml&base_date=" + today + "&base_time=0200&nx=59&ny=120";

		apiUrl += "?serviceKey=" + userKey + "&numOfRows=" + WEATHER_DATA_ROW_COUNT
				+ "&dataType=xml&base_date=" + today + "&base_time=0200&";

		GpsTransfer gpsCoord = new GpsTransfer(Double.valueOf(coX), Double.valueOf(coY));
		gpsCoord.transfer(gpsCoord, 0);
		apiUrl += "nx=" + (int) gpsCoord.getxLat() + "&ny=" + (int) gpsCoord.getyLon();

		try {
			URL url = URI.create(apiUrl).toURL();
			for (int attempt = 0; attempt < WEATHER_API_RETRY_LIMIT; attempt++) {
				try (InputStream is = url.openStream()) {
					result = parsingXMLData(is, locate, today);
				}
				if (!result.startsWith("서비스키를")) {
					break;
				}
			}
		} catch (IOException e1) {
			result = e1.toString();
			log.error("Error while calling weather API", e1);
		}
		log.info("Weather result: {}", result.replace("\n", " "));

		return result;
	}

	public String getNewsTopTen() {

		String result = "";
		try {
			StringBuilder sb = new StringBuilder();
			org.jsoup.nodes.Document doc = Jsoup.connect("https://www.mk.co.kr/news/economy/").get();
			Elements newsTitles = doc.select(".best_view_news_wrap .news_ttl");
			for (int i = 0; i < newsTitles.size(); i++) {
				sb.append(",  ,  ,  ");
				sb.append(i + 1);
				sb.append(". ");
				org.jsoup.nodes.Element title = newsTitles.get(i);
				String str = title.text();
				if (str.contains("회원용")) {
					str = str.substring(str.indexOf("회원용"));
				}
				sb.append(str);
				if (i == 12) {
					break;
				}
				sb.append("\n");
			}
			result = sb.toString();
		} catch (IOException e) {
			result = "에러 발생";
		}

		return result;
	}

	private String parsingXMLData(InputStream is, String locate, String baseDate) {
		String lowTemp = "";
		String highTemp = "";
		String sky = "";
		String pty = "";
		try {
			int skyInt = 0;
			int ptyInt = 0;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			NodeList nList = document.getElementsByTagName("item");
			if (nList.getLength() == 0) {
				return "서비스키를 다시 확인하세요\n올바른 서비스키를 입력하였다면 다시 시도하세요";
			}
			for (int i = 0; i < nList.getLength(); i++) {
				Node item = nList.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) { // 빈노드 거르기
					Element eElement = (Element) item;
					String fcstDate = eElement.getElementsByTagName("fcstDate").item(0).getTextContent();
					String category = eElement.getElementsByTagName("category").item(0).getTextContent();
					String fcstValue = eElement.getElementsByTagName("fcstValue").item(0).getTextContent();
					if (fcstDate.equals(String.valueOf(Integer.parseInt(baseDate) + 1))) {
						break;
					}
					switch (category) {
					case "TMN":
						lowTemp = fcstValue;
						break;
					case "TMX":
						highTemp = fcstValue;
						break;
					case "SKY":
						skyInt += Integer.parseInt(fcstValue);
						break;
					case "PTY":
						ptyInt += Integer.parseInt(fcstValue);
					}
				}
			}
			skyInt /= 21;
			skyInt--;
			Boolean ptyChk = ptyInt % 21 > 0;
			ptyInt /= 21;
			ptyInt = ptyChk ? +1 : 0;
			sky = SKY_LABELS[skyInt];
			pty = ptyInt > 0 ? PTY_LABELS[ptyInt] + UMBRELLA_NOTICE : PTY_LABELS[ptyInt];

		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Failed to parse weather response", e);
		}

		StringBuilder sb = new StringBuilder();
		String month = baseDate.substring(4, 6);
		String day = baseDate.substring(6);
		sb.append("  ").append(month).append("월 ").append(day).append("일  \n").append(locate)
				.append("의 날씨입니다.  \n").append("최고기온은 ");
		sb.append(highTemp);
		sb.append("도 이며  \n최저기온은");
		sb.append(lowTemp);
		sb.append("도, \n날씨는 ");
		sb.append(sky);
		sb.append(" 이며  \n").append(pty);

		return sb.toString();
	}

	public void callIPandService(String callService, String callHost) {
		String formatedNow = LocalDate.now(SEOUL_ZONE).format(DATE_DASHED);
		String time = LocalTime.now(SEOUL_ZONE).format(TIME_HHMMSS);

		log.info("Service call logged [service: {}, date: {}, time: {}, host: {}]", callService, formatedNow, time,
				callHost);

	}

	private String resolveBaseDate() {
		String baseDate = LocalDate.now(SEOUL_ZONE).format(DATE_COMPACT);
		String time = LocalTime.now(SEOUL_ZONE).format(TIME_HHMM);
		if (Integer.parseInt(time) < 211) {
			baseDate = String.valueOf(Integer.parseInt(baseDate) - 1);
		}
		return baseDate;
	}

	public class GpsTransfer {

		private double lat; // gps로 반환받은 위도
		private double lon; // gps로 반환받은 경도

		private double xLat; // x좌표로 변환된 위도
		private double yLon; // y좌표로 변환된 경도

		public GpsTransfer() {
		}

		public GpsTransfer(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		public double getLat() {
			return lat;
		}

		public double getLon() {
			return lon;
		}

		public double getxLat() {
			return xLat;
		}

		public double getyLon() {
			return yLon;
		}

		public void setLat(double lat) {
			this.lat = lat;
		}

		public void setLon(double lon) {
			this.lon = lon;
		}

		public void setxLat(double xLat) {
			this.xLat = xLat;
		}

		public void setyLon(double yLon) {
			this.yLon = yLon;
		}

		// x,y좌표로 변환해주는것
		public void transfer(GpsTransfer gpt, int mode) {

			double RE = 6371.00877; // 지구 반경(km)
			double GRID = 5.0; // 격자 간격(km)
			double SLAT1 = 30.0; // 투영 위도1(degree)
			double SLAT2 = 60.0; // 투영 위도2(degree)
			double OLON = 126.0; // 기준점 경도(degree)
			double OLAT = 38.0; // 기준점 위도(degree)
			double XO = 43; // 기준점 X좌표(GRID)
			double YO = 136; // 기1준점 Y좌표(GRID)

			//
			// LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도, lng_Y:경도),
			// "TO_GPS"(좌표->위경도, lat_X:x, lng_Y:y) )
			//

			double DEGRAD = Math.PI / 180.0;
			double RADDEG = 180.0 / Math.PI;

			double re = RE / GRID;
			double slat1 = SLAT1 * DEGRAD;
			double slat2 = SLAT2 * DEGRAD;
			double olon = OLON * DEGRAD;
			double olat = OLAT * DEGRAD;

			double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
			sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
			double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
			sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
			double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
			ro = re * sf / Math.pow(ro, sn);

			if (mode == 0) {
//	            rs.lat = lat_X; //gps 좌표 위도
//	            rs.lng = lng_Y; //gps 좌표 경도
				double ra = Math.tan(Math.PI * 0.25 + (gpt.getLat()) * DEGRAD * 0.5);
				ra = re * sf / Math.pow(ra, sn);
				double theta = gpt.getLon() * DEGRAD - olon;
				if (theta > Math.PI)
					theta -= 2.0 * Math.PI;
				if (theta < -Math.PI)
					theta += 2.0 * Math.PI;
				theta *= sn;
				double x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
				double y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
				gpt.setxLat(x);
				gpt.setyLon(y);
//	            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
//	            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
			} else {
//	            rs.x = lat_X; //기존의 x좌표
//	            rs.y = lng_Y; //기존의 경도
				double xlat = gpt.getxLat();
				double ylon = gpt.getyLon();
				double xn = xlat - XO;
				double yn = ro - ylon + YO;
				double ra = Math.sqrt(xn * xn + yn * yn);
				if (sn < 0.0) {
					ra = -ra;
				}
				double alat = Math.pow((re * sf / ra), (1.0 / sn));
				alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

				double theta = 0.0;
				if (Math.abs(xn) <= 0.0) {
					theta = 0.0;
				} else {
					if (Math.abs(yn) <= 0.0) {
						theta = Math.PI * 0.5;
						if (xn < 0.0) {
							theta = -theta;
						}
					} else
						theta = Math.atan2(xn, yn);
				}
				double alon = theta / sn + olon;
//	            rs.lat = alat * RADDEG; //gps 좌표 위도
//	            rs.lng = alon * RADDEG; //gps 좌표 경도
				gpt.setLat(alat * RADDEG);
				gpt.setLon(alon * RADDEG);
			}
		}

		@Override
		public String toString() {
			return "GpsTransfer{" + "lat=" + lat + ", lon=" + lon + ", xLat=" + xLat + ", yLon=" + yLon + '}';
		}
	}
}