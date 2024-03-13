import json
import pytesseract
from flask import Flask, request, jsonify
from flask_cors import CORS
from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from bs4 import BeautifulSoup
import time
import pymysql
from PIL import Image
import io
import os
import uuid
import re
import googlemaps
import pandas as pd

app = Flask(__name__)
CORS(app)

# 전역 변수로 주소와 회사명을 초기화
address_data = ""
company_name_data = ""
dates_date = ""
session_value = ""


# Tesseract OCR 경로 설정
pytesseract.pytesseract.tesseract_cmd = r'C:/Program Files/Tesseract-OCR/tesseract'

@app.route('/img', methods=['POST'])
def handle_post_request():
    try:
        # 세션 값 읽기
        global session_value
        session_value = request.headers.get('Session')
        print(session_value)

        # 이미지 데이터 읽기
        image_data = request.get_data()

        # 이미지 데이터를 PIL Image로 변환
        image = Image.open(io.BytesIO(image_data))

        # byte 형식으로 넘어왔기에 저장하는 방식
        unique_filename = str(uuid.uuid4()) + '.jpg'
        save_image_path = os.path.join('path/to/save/', unique_filename)

        # 이미지 저장
        image.save(save_image_path)

        # 이미지를 다시 열어서 사용
        saved_image = Image.open(save_image_path)

        # 이미지에서 텍스트 추출
        text = pytesseract.image_to_string(saved_image, lang='kor+eng', config='--psm 4')
        print(text)
        print("1232132132132121")

        # 찾을 문자열
        certification_string = "도로명주소 :"

        # 문자열에서 인증 부분 제거
        result = text.replace(certification_string, "").strip()

        # 정규 표현식으로 각각의 숫자 패턴 찾기
        business_number_pattern = re.compile(r'\b(\d{3}-\d{2}-\d{5})\b')
        date_pattern_primary  = re.compile(r'\b(\d{2,4}/\d{1,2}/\d{1,2})\b')
        date_pattern_secondary  = re.compile(r'\b(\d{2,4}-\d{1,2}-\d{1,2})\b')

        # 각각의 패턴에 대해 일치하는 값 찾기
        business_numbers = business_number_pattern.findall(result)
        dates_primary = date_pattern_primary.findall(result)
        dates_secondary = date_pattern_secondary.findall(result)

        # 사용할 날짜 패턴 선택
        dates = dates_primary if dates_primary else dates_secondary

        print(business_numbers)
        print("asdasdsadsasadsa")
        print(dates)

        # Chrome WebDriver 경로 설정 (ChromeDriver 다운로드 후 경로 설정)
        chrome_driver_path = './webdriver/chromedriver.exe'

        # ChromeService 객체 생성
        chrome_service = ChromeService(executable_path=chrome_driver_path)

        # Chrome WebDriver 생성
        driver = webdriver.Chrome(service=chrome_service)

        db = pymysql.connect(
            host='175.114.130.25', port=3306, database='tourProject',
            user='scott', password='tiger'
        )

        sess_userid = session_value

        cursor = db.cursor()
        print("디비 연결 성공")

        # 결과 출력
        for match in business_numbers:
            print("사업자번호 : " + match)

            # 하이픈 제거
            match_without_hyphen = match.replace('-', '')

            # Selenium을 사용하여 웹 페이지에 사업자 번호 전송하여 정보 가져오기
            url = f'https://www.bizno.net/article/{match_without_hyphen}'

            driver.get(url)

            # 페이지가 로딩될 때까지 기다림 (최소 1초 )
            time.sleep(1)

            # Selenium을 통해 동적으로 로딩된 페이지 소스 코드 가져오기
            html_source = driver.page_source

            # BeautifulSoup을 사용하여 HTML 파싱
            soup = BeautifulSoup(html_source, 'html.parser')

            company_name = soup.select_one(
                'div.details > div.title.d-flex.flex-row.justify-content-between > div > a > h1').text
            print(company_name)

            # '회사주소'를 포함하는 <th> 태그를 찾고 그 부모 <tr> 태그를 찾습니다.
            address_row = soup.select_one('th:contains("회사주소")').find_parent('tr')

            # address_row 내에서 <td> 태그를 찾습니다.
            address_td = address_row.find('td')

            # <br> 태그를 기준으로 텍스트를 분리하고 첫 번째 주소만 선택합니다.
            addresses = address_td.stripped_strings  # 모든 텍스트를 가져옵니다.
            first_address = next(addresses, None)  # 첫 번째 주소를 가져옵니다.

            # 주소 텍스트를 가져옵니다.
            address = first_address if first_address else ""
            print(address)

            if address:
                print("회사주소: " + address)
            else:
                print("회사주소를 찾을 수 없습니다.")
            if company_name:
                print("회사 이름: " + company_name)
            if dates:
                print("날짜 : " + dates[0])

                # 날짜 출력
            global address_data, company_name_data, dates_date

            # 주소와 회사명 저장,영수증 날짜
            address_data = address
            company_name_data = company_name
            dates_date = [date.replace("\\", "") for date in dates]
        # 브라우저 닫기
        driver.quit()



        print("ㅇㅇㅇ")
        return jsonify({"message": "Success"})
    except Exception as e:
        print(f"An error occurred: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 위도 경도 찍기 위하여
@app.route('/get_data', methods=['GET'])
def get_data():
    global address_data, company_name_data  # 전역 변수로 선언

    my_key = "AIzaSyBGLeIFOpSgEeZLTModCPUaL-YY-SdRLOQ"
    maps = googlemaps.Client(key=my_key)

    # 주소 정보 가져오기
    address = address_data
    geocode_result = maps.geocode(address)

    # 필요한 데이터 추출
    formatted_address = geocode_result[0]['formatted_address']
    location = geocode_result[0]['geometry']['location']
    latitude = location['lat']
    longitude = location['lng']

    # 데이터프레임 생성
    data = {'위도': [latitude], '경도': [longitude]}
    df = pd.DataFrame(data)

    # 결과 출력
    print(df)

    response = jsonify({"latitude": latitude, "longitude": longitude, "company_name": company_name_data,"address": address,"dates_date":dates_date})
    print(response)
    return response

@app.after_request
def call_after_request(response):
    return response

# 영수증에 해당하는 가게에 리뷰들
@app.route('/select_data', methods=['GET'])
def select_data():
    global company_name_data
    try:
        if request.method == 'GET':
            # 데이터베이스 연결
            db = pymysql.connect(
                host='175.114.130.25', port=3306, database='tourProject',
                user='scott', password='tiger'
            )

            cursor = db.cursor()

            # 예시: company_name_data를 사용하여 해당 회사명에 대한 정보 조회
            sql = "SELECT receipt_name, receipt_review_content, receipt_date, user_id FROM receipt WHERE receipt_name = %s ORDER BY receipt_date DESC"
            cursor.execute(sql, (company_name_data,))

            # 결과 가져오기
            results = cursor.fetchall()
            print(results.__sizeof__())
            print("asdsadsadsa")
            result_count=results.__sizeof__()


            data = []
            for row in results:
                item = {
                    "receipt_name": row[0],
                    "receipt_review_content": row[1],
                    "receipt_date": row[2],
                    "user_id": row[3]
                }
                data.append(item)

            db.close()

            # JSON 형태로 반환
            return jsonify({"data": data,"result_count":result_count})
        else:
            return jsonify({"error": "Invalid request method"}), 400

    except Exception as e:
        print(f"An error occurred: {str(e)}")
        return jsonify({"error": "An error occurred while processing the request."}), 500

# 영수증에 해당하는 가게에 리뷰 insert
@app.route('/insert_review', methods=['POST'])
def insert_data():
    try:
        global session_value

        data = request.json
        shopname = data.get('shopname')
        txtContents = data.get('txtContents')
        reviewDate = data.get('reviewDate')
        shopaddr = data.get('shopaddr')

        print(shopname)
        print(txtContents)
        print(reviewDate)
        print(shopaddr)
        print(session_value + "세션값")

        db = pymysql.connect(
            host='175.114.130.25', port=3306, database='tourProject',
            user='scott', password='tiger'
        )

        cursor = db.cursor()

        print("asdsadsadsa")

        # 리뷰 삽입 쿼리
        sql = "INSERT INTO receipt (receipt_name, receipt_review_content, receipt_date, user_id, receipt_business_addr) VALUES (%s, %s, %s, %s, %s)"
        cursor.execute(sql, (shopname, txtContents, reviewDate, session_value, shopaddr))
        print("12321321321")

        # 변경사항 커밋
        db.commit()

        # 성공적인 응답
        return jsonify({"status": "success", "message": "Data inserted successfully"})
    except Exception as e:
        # 오류가 발생한 경우 롤백하고 오류 응답
        db.rollback()
        return jsonify({"status": "error", "message": str(e)})
    finally:
        # 연결과 커서 닫기
        cursor.close()
        db.close()

if __name__ == '__main__':
    app.run(host='175.114.130.21', port=5111, debug=True, use_reloader=False)
