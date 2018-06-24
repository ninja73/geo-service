import random

def generate_grid(min_lon, max_lon, min_lan, max_lan):
    file = open("./src/main/resources/grid.txt", "w")
    for x in range(min_lon, max_lon):
        for y in range(min_lan, max_lan):
            val = str(x) + ',' + str(y) + ',' + str(random.uniform(2.0, 30.0)) + '\n'
            file.write(val)

def generate_user(min_lon, max_lon, min_lan, max_lan):
    file = open("./src/main/resources/user_labels.txt", "w")
    for x in range(1, 1000000):
        val = str(x) + ',' + str(round(random.uniform(min_lon, max_lon), 4)) + ',' + str(round(random.uniform(min_lan, max_lan), 4)) + '\n'
        file.write(val)

if __name__ == '__main__':
    min_lon = -30
    max_lon = 30
    min_lan = -90
    max_lan = 90
    generate_grid(min_lon, max_lon, min_lan, max_lan)
    generate_user(min_lon, max_lon, min_lan, max_lan)