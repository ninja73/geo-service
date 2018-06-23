from faker import Faker
import random

fake = Faker()

def generate_grid(min_lon, max_lon, min_lan, max_lan):
    file = open("./src/main/resources/grid.txt", "w")
    for x in range(min_lon, max_lon):
        for y in range(min_lan, max_lan):
            val = '{0: >32}'.format((x - min_lon) * (abs(min_lan) + max_lan) + (y - min_lan)) + ',' + '{0: >32}'.format(x) + ',' + '{0: >32}'.format(y) + ',' + '{0: >32}'.format(random.uniform(2.0, 30.0)) + '\n'
            file.write(val)

def generate_user(lon, lan):
    file = open("./src/main/resources/user_labels.txt", "w")
    for x in range(1, 10000000):
        val = '{0: >32}'.format(x) + ',' + '{0: >32}'.format(fake.latitude() % lon) + ',' + '{0: >32}'.format(fake.longitude() % lan) + '\n'
        file.write(val)


if __name__ == '__main__':
    min_lon = -30
    max_lon = 30
    min_lan = -90
    max_lan = 90
    generate_grid(min_lon, max_lon, min_lan, max_lan)
    generate_user(max_lon, max_lan)