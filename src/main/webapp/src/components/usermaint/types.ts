export interface UserDto {
  userId?: number | null;
  name: string;
  fullname: string;
  password?: string, // only defined when adding or updating a user
  createDate?: string;
  lastLoginDate?: string;
  loggedInCount?: number;
}
