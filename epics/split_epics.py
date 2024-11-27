import os
import re

def split_epics(input_file, output_dir):
    # Create the output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)

    with open(input_file, 'r', encoding='utf-8') as file:
        content = file.read()

    # Regex pattern to identify each epic section
    # This pattern captures sections starting with '## Epic N: Title'
    epic_pattern = re.compile(r'(## Epic \d+: [^\n]+)([\s\S]*?)(?=## Epic \d+:|\Z)', re.MULTILINE)

    # Find all epic sections
    matches = epic_pattern.findall(content)

    for match in matches:
        epic_title = match[0].strip()
        epic_content = match[1].strip()

        # Generate a filename-friendly version of the epic title
        filename = re.sub(r'[^\w\- ]', '', epic_title)  # Remove special characters
        filename = filename.replace(' ', '_') + '.md'   # Replace spaces with underscores

        filepath = os.path.join(output_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as epic_file:
            # Write the epic title and its content
            epic_file.write(f'{epic_title}\n\n{epic_content}\n')

    print(f"Successfully split epics into folder: {output_dir}")

if __name__ == "__main__":
    # Define the path to the main epics.md file
    input_markdown = 'epics.md'  # Adjust the path if necessary

    # Define the output directory for individual epics
    output_directory = 'epics'    # Ensure this matches your desired structure

    split_epics(input_markdown, output_directory)
